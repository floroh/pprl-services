package de.unileipzig.dbs.pprl.service.linkageunit.controller;

import de.unileipzig.dbs.pprl.service.linkageunit.data.converter.BatchMatchProjectConverter;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.BatchMatchProjectDto;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.BatchMatchProjectExecutionDto;
import de.unileipzig.dbs.pprl.service.linkageunit.data.mongo.BatchMatchProject;
import de.unileipzig.dbs.pprl.service.linkageunit.services.AsynchronousBatchMatcherService;
import de.unileipzig.dbs.pprl.service.linkageunit.services.MatcherModificationService;
import de.unileipzig.dbs.pprl.service.linkageunit.services.ProjectService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static de.unileipzig.dbs.pprl.service.linkageunit.services.LinkImprovementService.CONFIG_PROJECT_ID_TO_REPORT_TO;

@RestController
@Tag(name = ProjectController.TAG, description = "Project-based batch matching ")
@RequestMapping(value = "project", produces = MediaType.APPLICATION_JSON_VALUE)
@Timed
@Slf4j
public class ProjectController {

  public static final String TAG = "Project-based batch matching";

  private final AsynchronousBatchMatcherService matcherService;

  private final ProjectService projectService;

  private final MatcherModificationService matcherModificationService;

  public ProjectController(
    AsynchronousBatchMatcherService matcherService,
    ProjectService projectService, MatcherModificationService matcherModificationService) {
    this.matcherService = matcherService;
    this.projectService = projectService;
    this.matcherModificationService = matcherModificationService;
  }

  @Operation(summary = "Create new linkage project", tags = TAG)
  @PostMapping("/")
  public BatchMatchProjectDto create(@RequestBody BatchMatchProjectDto projectDto) {
    BatchMatchProject project = projectService.add(projectDto);

    return BatchMatchProjectConverter.projectToDto(project);
  }

  @Operation(summary = "Get an existing linkage project", tags = TAG)
  @GetMapping("/{projectId}")
  public BatchMatchProjectDto get(@PathVariable String projectId,
    @RequestParam(required = false) String update) {
    if (update != null && update.equals("true")) {
      projectService.updateProjectPhases(new ObjectId(projectId));
    }
    try {
      return projectService.getProjectDto(new ObjectId(projectId));
    } catch (Exception e) {
      log.warn("Tried to get non-existing project {}", projectId);
      return null;
    }
  }

  @Operation(summary = "Get existing linkage projects", tags = TAG)
  @GetMapping("/findAll")
  public List<BatchMatchProjectDto> findAll(@RequestParam(required = false) String update) {
    if (update != null && update.equals("true")) {
      projectService.getAllProjects().forEach(
        p -> projectService.updateProjectPhases(new ObjectId(p.getProjectId()))
      );
    }
    return projectService.getAllProjects();
  }

  @Operation(summary = "Run (the next phase of) a linkage project", tags = TAG)
  @PostMapping("/run/{projectId}")
  public BatchMatchProjectDto runNext(@PathVariable String projectId) {
    ObjectId prjId = new ObjectId(projectId);
    matcherService.runNext(prjId);
    projectService.updateProjectPhases(prjId);
    return projectService.getProjectDto(prjId);
  }

  @Operation(summary = "Run a linkage project for new records", tags = TAG)
  @PostMapping("/runForNew/{projectId}")
  public BatchMatchProjectDto runForNew(@PathVariable String projectId) {
    ObjectId prjId = new ObjectId(projectId);
    matcherService.runForNewRecords(prjId);
    projectService.updateProjectPhases(prjId);
    return projectService.getProjectDto(prjId);
  }

  @Operation(summary = "Execute (parts of) a linkage project", tags = TAG)
  @PostMapping("/run")
  public BatchMatchProjectDto runParts(@RequestBody BatchMatchProjectExecutionDto executionDto) {
    ObjectId projectId = new ObjectId(executionDto.getProjectId());
    projectService.reset(projectId, executionDto.getFromState());
    matcherService.runTo(projectId, executionDto.getToState());
    projectService.updateProjectPhases(projectId);
    return projectService.getProjectDto(projectId);
  }

  @Operation(summary = "Delete a project", tags = TAG)
  @DeleteMapping("/{projectId}")
  public void delete(@PathVariable String projectId, @RequestParam(required = false) boolean deleteParents) {
    List<ObjectId> projectsToDelete = new ArrayList<>();
    projectsToDelete.add(new ObjectId(projectId));
    if (deleteParents) {
      log.debug("Delete parents of project {}", projectId);
      BatchMatchProject project = projectService.getProject(new ObjectId(projectId));
      while (true) {
        Optional<String> projectIdToReportToString = project.getConfigValue(CONFIG_PROJECT_ID_TO_REPORT_TO);
        if (projectIdToReportToString.isEmpty()) {
          break;
        }
        ObjectId projectIdToReportTo = new ObjectId(projectIdToReportToString.get());
        projectsToDelete.add(projectIdToReportTo);
        project = projectService.getProject(projectIdToReportTo);
      }
    }
    projectsToDelete.forEach(projectService::deleteProject);
  }

  @Operation(summary = "Delete all projects", tags = TAG)
  @DeleteMapping("/all")
  public void deleteAll() {
    projectService.deleteAllProjects();
  }

  @Operation(summary = "Reset a linkage project", tags = TAG)
  @PostMapping("/reset")
  public BatchMatchProjectDto reset(@RequestBody BatchMatchProjectExecutionDto executionDto) {
    ObjectId projectId = new ObjectId(executionDto.getProjectId());
    projectService.reset(projectId, executionDto.getFromState());
    projectService.updateProjectPhases(projectId);
    return projectService.getProjectDto(projectId);
  }

}

package de.unileipzig.dbs.pprl.service.linkageunit.controller;

import de.unileipzig.dbs.pprl.core.matcher.model.api.LinkageProcessDataSet;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordEncodingWishDto;
import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoRecordPair;
import de.unileipzig.dbs.pprl.service.linkageunit.data.converter.RecordPairDtoConverter;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.MatcherUpdateRequest;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.MatchingDto;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.RecordPairDto;
import de.unileipzig.dbs.pprl.service.linkageunit.data.mongo.BatchMatchProject;
import de.unileipzig.dbs.pprl.service.linkageunit.data.mongo.ProjectState;
import de.unileipzig.dbs.pprl.service.linkageunit.dataset.DatabaseLinkageProcessDataset;
import de.unileipzig.dbs.pprl.service.linkageunit.services.AsynchronousBatchMatcherService;
import de.unileipzig.dbs.pprl.service.linkageunit.services.MatcherModificationService;
import de.unileipzig.dbs.pprl.service.linkageunit.services.ProjectService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Timed
@Slf4j
@Tag(name = MultiLayerProtocolController.TAG, description = "Handling of multi layer protocols")
@RequestMapping(value = "protocol", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin
public class MultiLayerProtocolController {

  public static final String TAG = "Multi layer protocols";

  private final AsynchronousBatchMatcherService matcherService;
  private final ProjectService projectService;

  private final MatcherModificationService matcherModificationService;

  public MultiLayerProtocolController(AsynchronousBatchMatcherService matcherService,
    ProjectService projectService,
    MatcherModificationService matcherModificationService) {
    this.matcherService = matcherService;
    this.projectService = projectService;
    this.matcherModificationService = matcherModificationService;
  }


  @Operation(summary = "Update a matcher based on improved links", tags = TAG)
  @PostMapping("/matcher/update")
  public MatchingDto update(@RequestBody MatcherUpdateRequest request) {
    return matcherModificationService.update(request);
  }

  @Operation(summary = "Determine uncertain record pairs", tags = TAG)
  @GetMapping("/pairs/uncertain/{projectId}")
  public List<RecordPairDto> determineUncertainPairs(@PathVariable String projectId, @RequestParam(required =
    false, defaultValue = "-1") int limit) {
    log.info("Determining uncertain record pairs for project {}.", projectId);
    ObjectId prjId = new ObjectId(projectId);
    return matcherService.determineUncertainLinks(prjId, limit);
  }

  @Operation(summary = "Get record encoding wishes", tags = TAG)
  @GetMapping("/wishlist/{projectId}")
  public List<RecordEncodingWishDto> getWishes(@PathVariable String projectId,
    @RequestParam(defaultValue = "true") String create, @RequestParam(required = false) int limit) {
    log.info("Get encoding wishes for project {}.", projectId);
    ObjectId prjId = new ObjectId(projectId);
    List<RecordEncodingWishDto> wishes;
    if (create != null && create.equals("true")) {
      wishes = matcherService.createEncodingWishesForUncertainLinks(prjId);
    } else {
      wishes = matcherService.getLinkImprovementService().getEncodingWishes(prjId);
    }
    if (limit > 0) {
      limit = Math.min(limit, wishes.size());
      wishes = wishes.subList(0, limit);
    }
    return wishes;
  }

  //TODO Use Pair Ids instead of Record Ids: LinkImprovementService.getUncertainPairsForPairIds
//  @Operation(summary = "Get uncertain pairs for a collection of record ids", tags = TAG)
//  @PostMapping("/pairs/{projectId}")
//  public List<RecordPairDto> getPairs(@PathVariable String projectId,
//    @RequestBody Collection<RecordIdDto> recordIdDtos) {
//    log.info("Getting uncertain pairs for {} record ids", recordIdDtos.size());
//    Set<RecordId> recordIds = recordIdDtos.stream()
//      .map(AbstractRecordConverter::toRecordId)
//      .collect(Collectors.toSet());
//    return matcherService.getLinkImprovementService().getUncertainPairsForGivenRecordIds(
//      new ObjectId(projectId), recordIds);
//  }

  @Operation(summary = "Add record pairs", tags = TAG)
  @PostMapping("/pairs")
  public void addRecordPairs(@RequestBody Collection<RecordPairDto> pairDtos,
    @RequestParam boolean merge) {
    List<MongoRecordPair> mongoPairs = pairDtos.stream()
      .map(RecordPairDtoConverter::convertDtoToMongoRecordPair)
      .collect(Collectors.toList());
    List<ObjectId> projectIds =
      mongoPairs.stream().map(MongoRecordPair::getProjectId).distinct().collect(Collectors.toList());
    log.info("Adding {} record pairs to project {}.", mongoPairs.size(), projectIds);
    if (projectIds.size() == 1) {
      ObjectId projectId = projectIds.getFirst();
      BatchMatchProject project = projectService.getProject(projectId);
      int datasetId = project.getDatasetId();
      mongoPairs.forEach(mrp -> {
        mrp.getLeftRecord().setIdDataset(datasetId);
        mrp.getRightRecord().setIdDataset(datasetId);
      });
      if (merge) {
        projectService.mergeNewImprovedRecordPairs(projectId, mongoPairs);
      } else {
        projectService.replaceRecordPairs(projectId, mongoPairs);
      }
    } else {
      throw new IllegalArgumentException("Record pairs must belong to the same project");
    }
  }

  @Operation(summary = "Update record pairs", tags = TAG)
  @PutMapping("/pairs")
  public void updateRecordPairs(@RequestBody Collection<RecordPairDto> pairDtos) {
    List<MongoRecordPair> mongoPairs = pairDtos.stream()
      .map(RecordPairDtoConverter::convertDtoToMongoRecordPair)
      .collect(Collectors.toList());
    List<ObjectId> projectIds =
      mongoPairs.stream().map(MongoRecordPair::getProjectId).distinct().collect(Collectors.toList());
    log.info("Updating {} record pairs in project {}.", mongoPairs.size(), projectIds);
    if (projectIds.size() == 1) {
      ObjectId projectId = projectIds.getFirst();
      BatchMatchProject project = projectService.getProject(projectId);
      int datasetId = project.getDatasetId();
      mongoPairs = mongoPairs.parallelStream()
        .peek(mrp -> {
          mrp.getLeftRecord().setIdDataset(datasetId);
          mrp.getRightRecord().setIdDataset(datasetId);
//          mrp.getProperties().remove(LinkageProcessDataSet.NEW);
        })
        .peek(DatabaseLinkageProcessDataset::updateActiveProperty)
        .collect(Collectors.toList());
      projectService.mergeUpdatedRecordPairs(projectId, mongoPairs);
      projectService.updateProjectPhases(projectId);
    } else {
      throw new IllegalArgumentException("Record pairs must belong to the same project");
    }
  }

  @Operation(summary = "Reclassify active links", tags = TAG)
  @PostMapping("/pairs/reclassify/{projectId}")
  public void reclassify(@PathVariable String projectId) {
    log.info("Reclassifying pairs of project {}", projectId);
    matcherModificationService.reclassify(new ObjectId(projectId));
  }

  @Operation(summary = "Report links", tags = TAG)
  @PostMapping("/pairs/report/{projectId}")
  public int reportPairs(@PathVariable String projectId) {
    log.info("Reporting pairs");
    DatabaseLinkageProcessDataset dataset = projectService.getDataset(new ObjectId(projectId));
    return matcherService.getLinkImprovementService().reportClassifiedRecordPairs(dataset);
  }

  @Operation(summary = "Fetch and compare uncertain pairs from parent project for new records", tags = TAG)
  @PostMapping("/pairs/fetch/{projectId}")
  public void fetchPairs(@PathVariable String projectId) {
    log.info("Fetching and comparing pairs");
    ObjectId prjId = new ObjectId(projectId);
    matcherService.getLinkImprovementService().fetchUncertainPairsFromParent(prjId);
    matcherService.compareNewPairs(prjId);
    // Update project state to classification as the labels are already available from the upper layer
    BatchMatchProject project = projectService.getProject(prjId);
    project.setState(ProjectState.CLASSIFICATION);
    projectService.save(project);
    projectService.updateProjectPhases(prjId);
  }
}

package de.unileipzig.dbs.pprl.service.linkageunit.controller;

import de.unileipzig.dbs.pprl.core.matcher.classification.Classifier;
import de.unileipzig.dbs.pprl.core.matcher.linking.DefaultLinker;
import de.unileipzig.dbs.pprl.core.matcher.matcher.DatasetBasedBatchMatcher;
import de.unileipzig.dbs.pprl.core.matcher.matcher.Matcher;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordRequirementsDto;
import de.unileipzig.dbs.pprl.service.common.utils.LocalConfigHandlerUtils;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.BatchMatchProjectDto;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.MatcherIdDto;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.MatchingDto;
import de.unileipzig.dbs.pprl.service.linkageunit.services.MatcherProviderService;
import de.unileipzig.dbs.pprl.service.linkageunit.services.ProjectService;
import de.unileipzig.dbs.pprl.service.linkageunit.services.TransientBatchMatcherService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Timed
@Tag(name = ConfigController.TAG, description = "Manage matching configurations")
@RequestMapping(value = "/config", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin
@Slf4j
public class ConfigController {

  public static final String TAG = "Configuration management";

  private final ProjectService projectService;

  private final MatcherProviderService matcherProviderService;

  private final TransientBatchMatcherService transientBatchMatcherService;

  public ConfigController(
    ProjectService projectService, MatcherProviderService matcherProviderService,
    TransientBatchMatcherService transientBatchMatcherService) {
    this.projectService = projectService;
    this.matcherProviderService = matcherProviderService;
    this.transientBatchMatcherService = transientBatchMatcherService;
  }

  @Operation(summary = "Add a configuration", tags = TAG)
  @PostMapping("/")
  public void add(@RequestBody MatchingDto configuration) {
    matcherProviderService.add(configuration);
  }

  @Operation(summary = "Override an existing configuration", tags = TAG)
  @PutMapping("/")
  public void update(@RequestBody MatchingDto configuration) {
    matcherProviderService.update(configuration);
  }

  @Operation(summary = "Remove a configuration", tags = TAG)
  @DeleteMapping("/")
  public void remove(@RequestBody MatcherIdDto matcherIdDto) {
    matcherProviderService.remove(matcherIdDto);
    transientBatchMatcherService.removeMatcher(matcherIdDto);
  }

  @Operation(summary = "Remove unused configurations", tags = TAG)
  @DeleteMapping("/unused")
  public int remove(@RequestParam boolean dryRun) {
    List<String> usedMatcherList = projectService.getAllProjects().stream()
      .map(BatchMatchProjectDto::getMethod)
      .toList();
    List<MatcherIdDto> unusedMatcherIds = matcherProviderService.getAvailableMatchers().stream()
      .map(MatchingDto::getId)
      .filter(id -> !usedMatcherList.contains(id.getMethod()))
      .filter(id -> !id.getMethod().contains("/trained/"))
      .toList();
    if (dryRun) {
      log.info("Unused matchers ({}): {}", unusedMatcherIds.size(), unusedMatcherIds);
    } else {
      log.info("Removing {} unused matchers", unusedMatcherIds.size());
      unusedMatcherIds.forEach(this::remove);
    }
    return unusedMatcherIds.size();
  }

  @Operation(summary = "Get the configuration by its ID", tags = TAG)
  @PostMapping("/findById")
  public MatchingDto getMatching(@RequestBody MatcherIdDto idDto) {
    List<MatchingDto> selected = matcherProviderService.getAvailableMatchers().stream()
      .filter(config -> config.getId().getMethod().equals(idDto.getMethod()))
      .collect(Collectors.toList());
    if (selected.isEmpty()) {
      throw new RuntimeException("No matching found for " + idDto);
    }
    return selected.getFirst();
  }

  @Operation(summary = "Get a list of all configuration IDs", tags = TAG)
  @GetMapping("/findAll")
  public List<MatcherIdDto> getConfigs() {
    return matcherProviderService.getAvailableMatchers().stream()
      .map(MatchingDto::getId)
      .collect(Collectors.toList());
  }

  @Operation(summary = "Get descriptions of the requirements of all matching methods", tags = TAG)
  @GetMapping("/findAllRequirements")
  public List<RecordRequirementsDto> getMethodRequirements() {
    return matcherProviderService.getAvailableMatchers().stream()
      .map(matchingDto -> {
        RecordRequirementsDto validation = matchingDto.getValidation();
        if (validation != null) {
          validation.setMethod(matchingDto.getId().getMethod());
          LocalConfigHandlerUtils.addDisplayNames(validation);
        }
        return validation;
      })
      .collect(Collectors.toList());
  }

  @Operation(summary = "Get classifier description", tags = TAG)
  @PostMapping("/classifier")
  public MatchingDto getClassifierDescription(@RequestBody MatcherIdDto matcherId) {
    Matcher matcher = matcherProviderService.getMatcher(matcherId);
    String modelDescription = "NOT FOUND";
    try {
      DefaultLinker linker = (DefaultLinker) ((DatasetBasedBatchMatcher) matcher).getLinker();
      Classifier classifier = linker.getClassifier();
      modelDescription = classifier.getModelDescription();
    } catch (Exception e) {
      log.error("Failed to get classifier", e);
    }
    return MatchingDto.builder()
      .id(matcherId)
      .classifierDescription(modelDescription)
      .build();
  }
}
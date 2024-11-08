package de.unileipzig.dbs.pprl.service.linkageunit.controller;

import de.unileipzig.dbs.pprl.core.matcher.evaluation.GroundTruth;
import de.unileipzig.dbs.pprl.service.common.data.converter.RecordConverter;
import de.unileipzig.dbs.pprl.service.common.data.converter.RecordIdPairConverter;
import de.unileipzig.dbs.pprl.service.common.data.dto.GroundTruthDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordIdDto;
import de.unileipzig.dbs.pprl.service.common.services.DatasetDtoService;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.MatcherTrainingsRequest;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.MatchingDto;
import de.unileipzig.dbs.pprl.service.linkageunit.services.MatcherModificationService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@Timed
@Slf4j
@Tag(name = EvaluationController.TAG, description = "Evaluate linkage results")
@RequestMapping(value = "evaluation", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin
public class EvaluationController {

  public static final String TAG = "Linkage result evaluation";

  private final DatasetDtoService datasetDtoService;

  private final MatcherModificationService matcherModificationService;


  public EvaluationController(DatasetDtoService datasetDtoService,
    MatcherModificationService matcherModificationService) {
    this.datasetDtoService = datasetDtoService;
    this.matcherModificationService = matcherModificationService;
  }

  @Operation(summary = "Add ground truth for a dataset", tags = TAG)
  @PostMapping("/ground-truth")
  public void addGroundTruth(@RequestBody GroundTruthDto groundTruthDto) {
    log.info("Adding ground truth for dataset {}", groundTruthDto.getDatasetId());
    datasetDtoService.addGroundTruth(groundTruthDto);
    log.debug("Finished adding ground truth for dataset {}", groundTruthDto.getDatasetId());
  }

  @Operation(summary = "Add ground truth for a dataset based on record global ids", tags = TAG)
  @PostMapping("/ground-truth/{datasetId}")
  public void addGroundTruthFromGlobalIds(@PathVariable int datasetId, 
    @RequestBody List<RecordIdDto> recordIdDtos) {
    log.info("Generating ground truth based on global ids for dataset {}", datasetId);
    GroundTruth groundTruth = GroundTruth.createFromRecordId(
      recordIdDtos.stream()
        .map(RecordConverter::toRecordId)
        .collect(Collectors.toList())
    );

    GroundTruthDto groundTruthDto = GroundTruthDto.builder()
      .datasetId(datasetId)
      .recordIdPairs(groundTruth.getIdPairs().stream()
        .map(RecordIdPairConverter::toDto)
        .collect(Collectors.toList()))
      .build();
    addGroundTruth(groundTruthDto);
  }

  @Operation(summary = "Get ground truth for a dataset", tags = TAG)
  @GetMapping("/ground-truth/{datasetId}")
  public Optional<GroundTruthDto> fetch(@PathVariable int datasetId) {
    log.info("Fetching ground truth for dataset {}", datasetId);
    return datasetDtoService.getGroundTruth(datasetId);
  }

  @Operation(summary = "Fit a matcher for a dataset with ground truth", tags = TAG)
  @PostMapping("/matcher/fit")
  public MatchingDto fit(@RequestBody MatcherTrainingsRequest request) {
    return matcherModificationService.trainWithGroundTruth(request);
  }

}
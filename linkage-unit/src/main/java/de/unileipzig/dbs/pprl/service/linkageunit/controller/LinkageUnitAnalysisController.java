package de.unileipzig.dbs.pprl.service.linkageunit.controller;

import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import de.unileipzig.dbs.pprl.service.common.data.dto.analysis.AnalysisRequestDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.analysis.AnalysisResultDto;
import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoAnalysisResult;
import de.unileipzig.dbs.pprl.service.common.services.AnalysisService;
import de.unileipzig.dbs.pprl.service.linkageunit.data.converter.RecordPairDtoConverter;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.MatchResultAnalysisRequestDto;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.MatcherIdDto;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.MatchingDto;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.RecordPairDto;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.ResultRequest;
import de.unileipzig.dbs.pprl.service.linkageunit.services.LinkageResultAnalysisService;
import de.unileipzig.dbs.pprl.service.linkageunit.services.MatcherProviderService;
import de.unileipzig.dbs.pprl.service.linkageunit.services.ProjectService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController("linkageUnitAnalysisController")
@RequestMapping(value = "analysis", produces = MediaType.APPLICATION_JSON_VALUE)
@Timed
@Slf4j
@Tag(name = LinkageUnitAnalysisController.TAG, description = "Analyse linkage results")
@CrossOrigin
public class LinkageUnitAnalysisController {

  public static final String TAG = "Linkage result analysis";

  private final AnalysisService analysisService;

  private final LinkageResultAnalysisService linkageResultAnalysisService;

  private final MatcherProviderService matcherProviderService;

  private final ProjectService projectService;

  public LinkageUnitAnalysisController(AnalysisService analysisService,
    LinkageResultAnalysisService linkageResultAnalysisService,
    MatcherProviderService matcherProviderService, ProjectService projectService) {
    this.analysisService = analysisService;
    this.linkageResultAnalysisService = linkageResultAnalysisService;
    this.matcherProviderService = matcherProviderService;
    this.projectService = projectService;
  }

  @Operation(summary = "Run a specific dataset analysis type", tags = TAG)
  @PostMapping("/runValidation")
  public AnalysisResultDto runValidationAnalysis(@RequestBody AnalysisRequestDto requestDto) {
    if (MongoAnalysisResult.Type.VALIDATION.name().equals(requestDto.getType())) {
      Map<String, String> parameters = requestDto.getParameters();
      Optional<MatchingDto> method = matcherProviderService.getMatchingById(
        MatcherIdDto.builder().method(parameters.get("method")).build()
      );
      if (method.isPresent()) {
        return analysisService.validateDataSet(
          requestDto.getDatasetId(),
          method.get().getValidation()
        );
      } else {
        throw new RuntimeException("Unknown matching id");
      }
    }
    throw new IllegalArgumentException(
      "This endpoint is vor validation only, use /run for other analysis types");
  }

  @Operation(summary = "Run a specific linkage result analysis type", tags = TAG)
  @PostMapping("/eval")
  public AnalysisResultDto runLinkageResultAnalysis(@RequestBody MatchResultAnalysisRequestDto requestDto) {
    return linkageResultAnalysisService.runLinkResultAnalyzer(requestDto);
  }

  @Operation(summary = "Get record pairs", tags = TAG)
  @PostMapping("/pairs")
  public List<RecordPairDto> getPairs(@RequestBody ResultRequest request) {
    log.info("Received request for record pairs: {}", request);
    final ObjectId prjId = new ObjectId(request.getProjectId());
    List<RecordPair> pairs = null;
    if (request.getPairProperties() == null || request.getPairProperties().isEmpty()) {
      pairs = projectService.getRecordPairsNoRecords(prjId);
    } else {
      pairs = projectService.getRecordPairsFilteredByProperties(prjId, request.getPairProperties());
    }
    log.info("Fetched {} record pairs", pairs.size());
    pairs = projectService.addGroundTruthTag(prjId, pairs);
    return pairs.stream()
      .map(RecordPairDtoConverter::convertRecordPairToDto)
      .collect(Collectors.toList());
  }
}

package de.unileipzig.dbs.pprl.service.common.controller;

import de.unileipzig.dbs.pprl.core.common.monitoring.Tag;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordIdPairDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.analysis.AnalysisRequestDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.analysis.AnalysisResultDto;
import de.unileipzig.dbs.pprl.service.common.services.AnalysisService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping(value = "analysis", produces = MediaType.APPLICATION_JSON_VALUE)
@Timed
@Slf4j
@io.swagger.v3.oas.annotations.tags.Tag(name = AnalysisController.TAG, description = "Analyse records of a dataset")
@CrossOrigin
public class AnalysisController {

  public static final String TAG = "Dataset analysis";

  private final AnalysisService analysisService;

  public AnalysisController(AnalysisService analysisService) {
    this.analysisService = analysisService;
  }

  @Operation(summary = "Get a list of all supported analysis types", tags = TAG)
  @GetMapping("/findAll")
  public List<String> getAnalysisTypes() {
    return analysisService.getAvailableAnalysisTypes();
  }

  @Operation(summary = "Run a specific analysis type", tags = TAG)
  @PostMapping("/run")
  public AnalysisResultDto runAnalysis(@RequestBody AnalysisRequestDto requestDto) {
    if (requestDto.getDatasetId() == 0) {
      throw new RuntimeException("No dataset id given");
    }
    return analysisService.getAnalysisResult(requestDto);
  }

  @Operation(summary = "Add tags to database", tags = TAG)
  @PostMapping("/tag/{datasetId}")
  public void saveTags(@PathVariable long datasetId,
                                     @RequestBody Collection<Tag> tags) {
    analysisService.saveTags(datasetId, tags);
  }
  @Operation(summary = "Delete all tags of a dataset from database", tags = TAG)
  @DeleteMapping("/tag/{datasetId}")
  public void deleteTags(@PathVariable long datasetId) {
    analysisService.deleteTags(datasetId);
  }

  @Operation(summary = "Get tags from database", tags = TAG)
  @GetMapping("/tag/{datasetId}/{origin}")
  public Collection<Tag> getTagsByOrigin(@PathVariable long datasetId, @PathVariable String origin) {
    return analysisService.getTags(datasetId, origin);
  }

  @Operation(summary = "Get tags from database", tags = TAG)
  @GetMapping("/tag/{datasetId}")
  public Collection<Tag> getTags(@PathVariable long datasetId) {
    return analysisService.getTags(datasetId, null);
  }

  @Operation(summary = "Get tags for pairs", tags = TAG)
  @PostMapping("/pair-tags/{datasetId}")
  public Collection<Tag> runPairAnalysis(@PathVariable long datasetId, @RequestBody List<RecordIdPairDto> pairs) {
    return analysisService.runPairAnalysis(datasetId, pairs);
  }
}
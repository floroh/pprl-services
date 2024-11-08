package de.unileipzig.dbs.pprl.service.common.controller;

import de.unileipzig.dbs.pprl.service.common.data.dto.analysis.AnalysisRequestDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.analysis.AnalysisResultDto;
import de.unileipzig.dbs.pprl.service.common.services.AnalysisService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "analysis", produces = MediaType.APPLICATION_JSON_VALUE)
@Timed
@Slf4j
@Tag(name = AnalysisController.TAG, description = "Analyse records of a dataset")
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
}
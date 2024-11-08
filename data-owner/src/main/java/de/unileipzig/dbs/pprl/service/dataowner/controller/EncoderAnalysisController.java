package de.unileipzig.dbs.pprl.service.dataowner.controller;

import de.unileipzig.dbs.pprl.service.common.data.dto.EncodingDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.EncodingIdDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.analysis.AnalysisRequestDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.analysis.AnalysisResultDto;
import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoAnalysisResult;
import de.unileipzig.dbs.pprl.service.common.services.AnalysisService;
import de.unileipzig.dbs.pprl.service.dataowner.services.EncoderProviderService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController("dataOwnerAnalysisController")
@RequestMapping(value = "analysis", produces = MediaType.APPLICATION_JSON_VALUE)
@Timed
@Slf4j
@Tag(name = EncoderAnalysisController.TAG, description = "Analyse records of a dataset")
@CrossOrigin
public class EncoderAnalysisController {

  public static final String TAG = "Dataset analysis";

  private final AnalysisService analysisService;

  private final EncoderProviderService encoderProviderService;

  public EncoderAnalysisController(AnalysisService analysisService,
    EncoderProviderService encoderProviderService) {
    this.analysisService = analysisService;
    this.encoderProviderService = encoderProviderService;
  }

  @Operation(summary = "Run a specific analysis type", tags = TAG)
  @PostMapping("/runValidation")
  public AnalysisResultDto runValidationAnalysis(@RequestBody AnalysisRequestDto requestDto) {
    if (requestDto.getDatasetId() == 0) {
      throw new RuntimeException("No dataset id given");
    }
    if (MongoAnalysisResult.Type.VALIDATION.name().equals(requestDto.getType())) {
      Map<String, String> parameters = requestDto.getParameters();
      Optional<EncodingDto> method = encoderProviderService.getById(
        EncodingIdDto.builder().method(parameters.get("method")).build()
      );
      if (method.isPresent()) {
        return analysisService.validateDataSet(
          requestDto.getDatasetId(),
          method.get().getValidation()
        );
      } else {
        throw new RuntimeException("Unknown encoding method");
      }
    }
    throw new IllegalArgumentException(
      "This endpoint is vor validation only, use /run for other analysis types");
  }
}
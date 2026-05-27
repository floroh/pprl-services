package de.unileipzig.dbs.pprl.service.linkageunit.services;

import com.jayway.jsonpath.PathNotFoundException;
import de.unileipzig.dbs.pprl.core.analyzer.AnalysisResult;
import de.unileipzig.dbs.pprl.core.analyzer.DataSetAnalyzer;
import de.unileipzig.dbs.pprl.core.analyzer.attribute.AttributeLength;
import de.unileipzig.dbs.pprl.core.analyzer.record.WeightAnalyzer;
import de.unileipzig.dbs.pprl.core.analyzer.results.Result;
import de.unileipzig.dbs.pprl.core.analyzer.results.ResultSet;
import de.unileipzig.dbs.pprl.core.common.exceptions.PprlException;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.matcher.MatcherSerialization;
import de.unileipzig.dbs.pprl.core.matcher.matcher.Matcher;
import de.unileipzig.dbs.pprl.service.common.data.converter.RecordConverter;
import de.unileipzig.dbs.pprl.service.common.modifier.JsonModifier;
import de.unileipzig.dbs.pprl.service.common.services.DatasetDtoService;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.MatcherIdDto;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.MatchingCreationRequestDto;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.MatchingDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.unileipzig.dbs.pprl.core.analyzer.Analyzer.HEADER_ATTRIBUTE;
import static de.unileipzig.dbs.pprl.core.analyzer.DataSetAnalyzer.RECORD_GROUP_ALL;
import static de.unileipzig.dbs.pprl.core.analyzer.record.WeightAnalyzer.M_WEIGHT;
import static de.unileipzig.dbs.pprl.core.analyzer.record.WeightAnalyzer.U_WEIGHT;
import static de.unileipzig.dbs.pprl.core.analyzer.record.WeightAnalyzer.WEIGHT;

@Service
@Slf4j
public class MatchingCreationService {

  private static String JSONPATH_WEIGHTED_SIMILARITY_AGGREGATOR = "$.." +
          JsonModifier.classSelector("similarityAggregator", "WeightedSimilarityAggregator") +
          ".weights";
  private static String JSONPATH_SCALED_WEIGHT_CALCULATOR = "$.." +
          JsonModifier.classSelector("weightCalculator", "ScaledWeightCalculator") +
          ".defaultWeights";
  private static String JSONPATH_FELLEGI_SUNTER_M_WEIGHTS = "$.." +
          JsonModifier.classSelector("similarityAggregator", "FellegiSunterSimilarityAggregator") +
          ".mWeights";
  private static String JSONPATH_FELLEGI_SUNTER_U_WEIGHTS = "$.." +
          JsonModifier.classSelector("similarityAggregator", "FellegiSunterSimilarityAggregator") +
          ".uWeights";

  private final MatcherProviderService matcherProviderService;

  private final DatasetDtoService datasetDtoService;

  public MatchingCreationService(MatcherProviderService matcherProviderService,
                                 DatasetDtoService datasetDtoService) {
    this.matcherProviderService = matcherProviderService;
    this.datasetDtoService = datasetDtoService;
  }

  public MatchingDto createMatching(MatchingCreationRequestDto requestDto) {
    updateRequest(requestDto);
    log.info("Creating matching based on request: {}", requestDto);
    Matcher baseMatcher = matcherProviderService.getMatcher(requestDto.getBaseMatcherId());
    String newConfig = MatcherSerialization.serializeJson(baseMatcher);
    newConfig = updateMatcherConfig(newConfig, requestDto);
    log.debug("New encoder: {}", baseMatcher);
    MatcherIdDto newIdDto = null;
    if (requestDto.getOutputMatcherId() != null) {
      newIdDto = requestDto.getOutputMatcherId();
    } else {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
      String timestamp = LocalDateTime.now().format(formatter);
      newIdDto = new MatcherIdDto(
        requestDto.getBaseMatcherId().getMethod() + "_" + timestamp,
        requestDto.getBaseMatcherId().getProject()
      );
    }
    MatchingDto newMatching = MatchingDto.builder()
      .config(newConfig)
      .id(newIdDto)
      .build();

    if (requestDto.isPersist()) {
      matcherProviderService.add(newMatching);
    }
    return newMatching;
  }

  private void updateRequest(MatchingCreationRequestDto requestDto) {
    boolean buildAttributeWeights = (requestDto.getAttributeWeights() == null
      || requestDto.getAttributeWeights().isEmpty());

    if (buildAttributeWeights) {
      log.info("Missing attributeLength. Computing based on dataset {}", requestDto.getDatasetId());
      RecordConverter converter = new RecordConverter();
      List<Record> records = datasetDtoService.getRecordsWithGlobalIdIfAvailable(requestDto.getDatasetId())
        .stream()
        .map(converter::toRecord)
        .toList();
      DataSetAnalyzer dsa = new DataSetAnalyzer();
      dsa.setRunPerSource(false);
      dsa.addAnalyzer(new WeightAnalyzer());
      AnalysisResult analysisResult = dsa.run(records);
      List<ResultSet> resultSets = analysisResult.getResults().get(RECORD_GROUP_ALL);

      Optional<ResultSet> alResult = resultSets.stream()
        .filter(rs -> rs.getName().equals("WeightAnalyzer")).findFirst();
      if (alResult.isPresent()) {
        Map<String, Double> attributeWeights = new HashMap<>();
        Map<String, Double> attributeMWeights = new HashMap<>();
        Map<String, Double> attributeUWeights = new HashMap<>();
        for (Result result : alResult.get().getResults()) {
          String attributeName = result.getParams().get(HEADER_ATTRIBUTE);
          attributeWeights.put(attributeName, result.getMetrics().get(WEIGHT).doubleValue());
          attributeMWeights.put(attributeName, result.getMetrics().get(M_WEIGHT).doubleValue());
          attributeUWeights.put(attributeName, result.getMetrics().get(U_WEIGHT).doubleValue());
        }
        if (attributeWeights.isEmpty()) {
          throw new PprlException("Failed to extract attribute weights statistics from dataset "
            + requestDto.getDatasetId());
        }
        requestDto.setAttributeWeights(attributeWeights);
        requestDto.setAttributeMWeights(attributeMWeights);
        requestDto.setAttributeUWeights(attributeUWeights);
      }
    }
  }

  private static String updateMatcherConfig(String config, MatchingCreationRequestDto request) {
    int numberOfUpdates = 0;
    for (Map.Entry<String, Double> weightEntry : request.getAttributeWeights().entrySet()) {
      String attributeName = weightEntry.getKey();
      Double value = weightEntry.getValue();
      log.info("Updating attribute weight for attribute {}", attributeName);
      boolean currentUpdate = false;
      try {
        config = JsonModifier.put(config, JSONPATH_SCALED_WEIGHT_CALCULATOR, attributeName, value);
        currentUpdate = true;
      } catch (PathNotFoundException ignored) {
      }
      try {
        config = JsonModifier.put(config, JSONPATH_WEIGHTED_SIMILARITY_AGGREGATOR, attributeName, value);
        currentUpdate = true;
      } catch (PathNotFoundException ignored) {
      }
      if (!currentUpdate) {
        log.warn("Tried to modify property (weight) that does not exist");
      } else {
        numberOfUpdates++;
      }
    }
    if (request.getAttributeMWeights() != null && request.getAttributeUWeights() != null) {
      for (Map.Entry<String, Double> mweightEntry : request.getAttributeMWeights().entrySet()) {
        String attributeName = mweightEntry.getKey();
        log.info("Updating Fellegi Sunter m and u weights for attribute {}", attributeName);
        Double mWeight = mweightEntry.getValue();
        Double uWeight = request.getAttributeUWeights().get(attributeName);
        boolean currentUpdate = true;
        try {
          config = JsonModifier.put(config, JSONPATH_FELLEGI_SUNTER_M_WEIGHTS, attributeName, mWeight);
        } catch (PathNotFoundException ignored) {
          currentUpdate = false;
        }
        try {
          config = JsonModifier.put(config, JSONPATH_FELLEGI_SUNTER_U_WEIGHTS, attributeName, uWeight);
        } catch (PathNotFoundException ignored) {
          currentUpdate = false;
        }
        if (!currentUpdate) {
          log.warn("Tried to modify property (m- and u-weight) that does not exist");
        } else {
          numberOfUpdates++;
        }
      }
    }
    log.info("Updated {} attribute configurations", numberOfUpdates);
    return config;
  }
}

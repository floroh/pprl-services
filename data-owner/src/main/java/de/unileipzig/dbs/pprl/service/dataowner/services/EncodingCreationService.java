package de.unileipzig.dbs.pprl.service.dataowner.services;

import com.jayway.jsonpath.PathNotFoundException;
import de.unileipzig.dbs.pprl.core.analyzer.AnalysisResult;
import de.unileipzig.dbs.pprl.core.analyzer.DataSetAnalyzer;
import de.unileipzig.dbs.pprl.core.analyzer.attribute.AttributeLength;
import de.unileipzig.dbs.pprl.core.analyzer.record.WeightAnalyzer;
import de.unileipzig.dbs.pprl.core.analyzer.results.Result;
import de.unileipzig.dbs.pprl.core.analyzer.results.ResultSet;
import de.unileipzig.dbs.pprl.core.common.RecordUtils;
import de.unileipzig.dbs.pprl.core.common.exceptions.PprlException;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordId;
import de.unileipzig.dbs.pprl.core.encoder.RecordEncoderSerialization;
import de.unileipzig.dbs.pprl.core.encoder.record.RecordEncoder;
import de.unileipzig.dbs.pprl.core.encoder.record.SourceSpecificEncoder;
import de.unileipzig.dbs.pprl.service.common.data.converter.RecordConverter;
import de.unileipzig.dbs.pprl.service.common.data.dto.EncodingDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.EncodingIdDto;
import de.unileipzig.dbs.pprl.service.common.modifier.JsonModifier;
import de.unileipzig.dbs.pprl.service.common.services.DatasetDtoService;
import de.unileipzig.dbs.pprl.service.dataowner.data.dto.EncodingCreationRequestDto;
import de.unileipzig.dbs.pprl.service.dataowner.data.dto.EncodingCreationResponseDto;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static de.unileipzig.dbs.pprl.core.analyzer.Analyzer.HEADER_ATTRIBUTE;
import static de.unileipzig.dbs.pprl.core.analyzer.DataSetAnalyzer.RECORD_GROUP_ALL;
import static de.unileipzig.dbs.pprl.core.analyzer.record.WeightAnalyzer.WEIGHT;

@Service
@Slf4j
public class EncodingCreationService {

  public static final String JSON_FREQUENCY_LOOKUP_PROVIDER_DB_DATASOURCE = "$.." + JsonModifier.fullClassSelector("frequencyLookupProvider",
          "de.unileipzig.dbs.pprl.service.common.dataset.MongoAttributesFrequencyLookupProvider")
          + ".datasetSource";
  public static final String JSON_FREQUENCY_LOOKUP_PROVIDER_DB_DATASETID = "$.." + JsonModifier.fullClassSelector("frequencyLookupProvider",
          "de.unileipzig.dbs.pprl.service.common.dataset.MongoAttributesFrequencyLookupProvider")
          + ".datasetId";

  private final EncoderProviderService encoderProviderService;

  private final DatasetDtoService datasetDtoService;

  public EncodingCreationService(EncoderProviderService encoderProviderService,
                                 DatasetDtoService datasetDtoService) {
    this.encoderProviderService = encoderProviderService;
    this.datasetDtoService = datasetDtoService;
  }

  public EncodingCreationResponseDto createEncoding(EncodingCreationRequestDto requestDto) {
    updateRequest(requestDto);
    log.info("Creating encoding based on request: {}", requestDto);
    RecordEncoder baseEncoder = encoderProviderService.getEncoder(requestDto.getBaseEncodingId());
    String newEncoderConfig = RecordEncoderSerialization.serializeJson(baseEncoder, false);
    newEncoderConfig = updateEncoder(newEncoderConfig, requestDto);
    log.debug("New encoder: {}", RecordEncoderSerialization.deserializeJsonSafe(newEncoderConfig));
    EncodingIdDto newIdDto = null;
    if (requestDto.getOutputEncodingId() != null) {
      newIdDto = requestDto.getOutputEncodingId();
    } else {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
      String timestamp = LocalDateTime.now().format(formatter);
      newIdDto = new EncodingIdDto(
              requestDto.getBaseEncodingId().getMethod() + "_" + timestamp,
              requestDto.getBaseEncodingId().getProject()
      );
    }
    EncodingDto newEncoding = EncodingDto.builder()
            .config(newEncoderConfig)
            .id(newIdDto)
            .build();

    if (requestDto.isPersist()) {
      encoderProviderService.addEncoding(newEncoding);
    }
    return EncodingCreationResponseDto.builder()
            .request(requestDto)
            .encoding(newEncoding)
            .build();
  }

  private void updateRequest(EncodingCreationRequestDto requestDto) {
    boolean isNoneWeightSelection = requestDto.getWeightSelectionMethod().equals(EncodingCreationRequestDto.WeightSelectionMethod.NONE);
    boolean buildAttributeWeights = requestDto.getAttributeWeights() == null && !isNoneWeightSelection;
    boolean buildAttributeLength = requestDto.getAttributeLength() == null &&
            (buildAttributeWeights || requestDto.getAttributeWeights() != null);
    boolean buildSources = requestDto.isSourceSpecific() && requestDto.getSources() == null;
    boolean sourceSpecific = requestDto.isSourceSpecific();
    if (sourceSpecific) {
      buildAttributeWeights = requestDto.getSourceSpecificAttributeWeights() == null &&
              requestDto.getAttributeWeights() == null && !isNoneWeightSelection;
      buildAttributeLength = requestDto.getSourceSpecificAttributeLength() == null &&
              (buildAttributeWeights || requestDto.getAttributeWeights() != null || requestDto.getSourceSpecificAttributeWeights() != null);
    }

    if (buildAttributeLength || buildAttributeWeights || buildSources) {
      log.info("Computing attribute length and/or weights based on dataset {}", requestDto.getDatasetId());
      RecordConverter converter = new RecordConverter();
      List<Record> records = datasetDtoService.getRecordsWithGlobalIdIfAvailable(requestDto.getDatasetId())
              .stream()
              .map(converter::toRecord)
              .toList();
      DataSetAnalyzer dsa = new DataSetAnalyzer();
      dsa.setRunPerSource(sourceSpecific);
      if (buildAttributeLength) {
        dsa.addAnalyzer(new AttributeLength());
      }
      if (buildAttributeWeights && !sourceSpecific) {
        dsa.addAnalyzer(new WeightAnalyzer());
      }
      AnalysisResult analysisResult = dsa.run(records);
      if (sourceSpecific) {
        if (buildSources) {
          Map<String, List<Record>> groupedRecords = RecordUtils.groupById(records, RecordId.SOURCE_ID);
          requestDto.setSources(new ArrayList<>(groupedRecords.keySet()));
        }
        if (buildAttributeLength) {
          Map<String, Map<String, Double>> sourceSpecificAttributeLengths = new HashMap<>();
          for (Map.Entry<String, List<ResultSet>> stringListEntry : analysisResult.getResults().entrySet()) {
            String source = stringListEntry.getKey();
            if (source.equals(RECORD_GROUP_ALL)) continue;
            Map<String, Double> attributeLengths = getAttributeLength(stringListEntry.getValue());
            if (attributeLengths.isEmpty()) {
              throw new PprlException("Failed to extract attribute length statistics from dataset "
                      + requestDto.getDatasetId());
            }
            sourceSpecificAttributeLengths.put(source, attributeLengths);
          }
          log.info("Computed source-specific attribute length: {}", sourceSpecificAttributeLengths);
          requestDto.setSourceSpecificAttributeLength(sourceSpecificAttributeLengths);
        }
        if (buildAttributeWeights) {
          Map<String, List<Record>> groupedRecords = RecordUtils.groupById(records, RecordId.SOURCE_ID);
          requestDto.setSources(new ArrayList<>(groupedRecords.keySet()));
          Map<String, Map<String, Double>> sourceSpecificAttributeWeights = new HashMap<>();
          Map<String, Double> errorRates = requestDto.getAttributeErrorRates();
          if (errorRates == null) {
            throw new PprlException("attributeErrorRates must be given for generating source-specific weights.");
          }
          WeightAnalyzer analyzer = new WeightAnalyzer(errorRates);
          for (Map.Entry<String, List<Record>> stringListEntry : groupedRecords.entrySet()) {
            ResultSet resultSet = analyzer.analyze(stringListEntry.getValue());
            String source = stringListEntry.getKey();
            Map<String, Double> attributeWeights = getAttributeWeights(List.of(resultSet));
            if (attributeWeights.isEmpty()) {
              throw new PprlException("Failed to extract attribute weight statistics from dataset "
                      + requestDto.getDatasetId());
            }
            sourceSpecificAttributeWeights.put(source, attributeWeights);
          }
          log.info("Computed source-specific attribute weights: {}", sourceSpecificAttributeWeights);
          requestDto.setSourceSpecificAttributeWeights(sourceSpecificAttributeWeights);
        }
        if (requestDto.getSourceSpecificAttributeWeights() == null && requestDto.getAttributeWeights() != null) {
          Map<String, Map<String, Double>> sourceSpecificAttributeWeights = new HashMap<>();
          for (String source : requestDto.getSources()) {
            sourceSpecificAttributeWeights.put(source, requestDto.getAttributeWeights());
          }
          log.info("Using default attribute weights as source-specific attribute weights: {}", sourceSpecificAttributeWeights);
          requestDto.setSourceSpecificAttributeWeights(sourceSpecificAttributeWeights);
        }
      } else {
        List<ResultSet> resultSets = analysisResult.getResults().get(RECORD_GROUP_ALL);
        if (buildAttributeLength) {
          Map<String, Double> attributeLengths = getAttributeLength(resultSets);
          if (attributeLengths.isEmpty()) {
            throw new PprlException("Failed to extract attribute length statistics from dataset "
                    + requestDto.getDatasetId());
          }
          log.info("Computed attribute length: {}", attributeLengths);
          requestDto.setAttributeLength(attributeLengths);
        }
        if (buildAttributeWeights) {
          Map<String, Double> attributeWeights = getAttributeWeights(resultSets);
          if (attributeWeights.isEmpty()) {
            throw new PprlException("Failed to extract attribute weights statistics from dataset "
                    + requestDto.getDatasetId());
          }
          log.info("Computed attribute weights: {}", attributeWeights);
          requestDto.setAttributeWeights(attributeWeights);
        }
      }
    }
  }

  private Map<String, Double> getAttributeLength(List<ResultSet> resultSets) {
    Optional<ResultSet> alResult = resultSets.stream()
            .filter(rs -> rs.getName().equals("AttributeLength")).findFirst();
    Map<String, Double> attributeLengths = new HashMap<>();
    if (alResult.isPresent()) {
      for (Result result : alResult.get().getResults()) {
        String attributeName = result.getParams().get(HEADER_ATTRIBUTE);
        BigDecimal attributeLength = result.getMetrics().get("mean");
        attributeLengths.put(attributeName, attributeLength.doubleValue());
      }
    }
    return attributeLengths;
  }

  private Map<String, Double> getAttributeWeights(List<ResultSet> resultSets) {
    Optional<ResultSet> alResult = resultSets.stream()
            .filter(rs -> rs.getName().equals("WeightAnalyzer")).findFirst();
    Map<String, Double> attributeWeights = new HashMap<>();
    if (alResult.isPresent()) {
      for (Result result : alResult.get().getResults()) {
        String attributeName = result.getParams().get(HEADER_ATTRIBUTE);
        BigDecimal attributeWeight = result.getMetrics().get(WEIGHT);
        attributeWeights.put(attributeName, attributeWeight.doubleValue());
      }
    }
    return attributeWeights;
  }

  private Map<String, Double> computeNumberOfHashFunctionsBasedOnWeights(
          Map<String, Double> weights,
          Map<String, Double> averageLength,
          Set<String> attributesInEncoding,
          boolean withPadding,
          double maxFillrate,
          int bloomFilterSize
  ) {
    Map<String, Double> tokens = new HashMap<>(averageLength);
    if (withPadding) {
      tokens.replaceAll((s, v) -> v + 1);
    }

    Set<String> relevantAttributes = weights.entrySet().stream()
            .filter(e -> e.getValue() != 0)
            .map(Map.Entry::getKey)
            .filter(attributesInEncoding::contains)
            .collect(Collectors.toSet());
    log.info("Attributes relevant for computation of number of hash functions: {}", relevantAttributes);

    double tokenSum = tokens.entrySet().stream()
            .filter(e -> relevantAttributes.contains(e.getKey()))
            .map(Map.Entry::getValue)
            .mapToDouble(Double::doubleValue).sum();
    double weightSum = weights.entrySet().stream()
            .filter(e -> relevantAttributes.contains(e.getKey()))
            .map(Map.Entry::getValue)
            .mapToDouble(Double::doubleValue).sum();
    double kForMaxFillrate = bloomFilterSize * maxFillrate / tokenSum;
    kForMaxFillrate = Math.round(kForMaxFillrate);

    Map<String, Double> numberOfHashFunctions = new HashMap<>();
    for (String s : weights.keySet()) {
      if (!tokens.containsKey(s)) continue;
      double k = weights.get(s) * kForMaxFillrate * tokenSum / (weightSum * tokens.get(s));
      numberOfHashFunctions.put(s, k);
    }
    return numberOfHashFunctions;
  }

  private String updateEncoder(String encoderConfig, EncodingCreationRequestDto requestDto) {
    if (requestDto.isSourceSpecific()) {
      Map<String, RecordEncoder> sourceEncoders = new HashMap<>();
//      if (requestDto.getWeightSelectionMethod() != EncodingCreationRequestDto.WeightSelectionMethod.NONE) {
      if (requestDto.getSourceSpecificAttributeWeights() != null) {
        for (Map.Entry<String, Map<String, Double>> weightEntry : requestDto.getSourceSpecificAttributeWeights().entrySet()) {
          String source = weightEntry.getKey();
          Map<String, Double> weights = weightEntry.getValue();
          Map<String, Double> lengths = requestDto.getSourceSpecificAttributeLength().get(source);
          if (weights == null || lengths == null) {
            throw new PprlException("Missing weights or lengths for updating encoder");
          }
          Set<String> includedAttributes = getIncludedAttributes(encoderConfig, weights.keySet());
          Map<String, Double> numberOfHashFunctionsPerAttribute = computeNumberOfHashFunctionsBasedOnWeights(
                  weights,
                  lengths,
                  includedAttributes,
                  true,
                  requestDto.getMaxFillrate(),
                  requestDto.getBloomFilterSize()
          );
          String sourceSpecificEncoderConfig = updateEncoderNumberOfHashFunctions(encoderConfig, numberOfHashFunctionsPerAttribute);
          try {
            RecordEncoder sourceSpecificEncoder = RecordEncoderSerialization.deserializeJson(sourceSpecificEncoderConfig);
            sourceEncoders.put(source, sourceSpecificEncoder);
          } catch (IOException e) {
            throw new PprlException("Failed to parse RecordEncoder with " + e.fillInStackTrace());
          }
        }
      }
      if (requestDto.getFrequencySelectionMethod() == EncodingCreationRequestDto.FrequencySelectionMethod.DATABASE) {
        for (String source : requestDto.getSources()) {
          if (sourceEncoders.containsKey(source)) {
            encoderConfig = RecordEncoderSerialization.serializeJson(sourceEncoders.get(source), false);
          }
          String sourceSpecificEncoderConfig = updateFrequencyProviderSource(encoderConfig, source);
          sourceSpecificEncoderConfig = updateFrequencyProviderDataset(sourceSpecificEncoderConfig, requestDto.getDatasetId());
          try {
            RecordEncoder sourceSpecificEncoder = RecordEncoderSerialization.deserializeJson(sourceSpecificEncoderConfig);
            sourceEncoders.put(source, sourceSpecificEncoder);
          } catch (IOException e) {
            throw new PprlException("Failed to parse RecordEncoder with " + e.fillInStackTrace());
          }
        }
      }
      SourceSpecificEncoder newEncoder = new SourceSpecificEncoder(sourceEncoders);
      log.info("Build SourceSpecificEncoder with differences: {}", newEncoder.getDifferences());
      return RecordEncoderSerialization.serializeJson(newEncoder, false);
    } else {
//      if (requestDto.getWeightSelectionMethod() != EncodingCreationRequestDto.WeightSelectionMethod.NONE) {
      if (requestDto.getAttributeWeights() != null) {
        Set<String> includedAttributes = getIncludedAttributes(encoderConfig, requestDto.getAttributeWeights().keySet());
        Map<String, Double> numberOfHashFunctionsPerAttribute = computeNumberOfHashFunctionsBasedOnWeights(
                requestDto.getAttributeWeights(),
                requestDto.getAttributeLength(),
                includedAttributes,
                true,
                requestDto.getMaxFillrate(),
                requestDto.getBloomFilterSize()
        );
        encoderConfig = updateEncoderNumberOfHashFunctions(encoderConfig, numberOfHashFunctionsPerAttribute);
      }
      if (requestDto.getFrequencySelectionMethod() == EncodingCreationRequestDto.FrequencySelectionMethod.DATABASE) {
        encoderConfig = updateFrequencyProviderDataset(encoderConfig, requestDto.getDatasetId());
      }
    }
    return encoderConfig;
  }

  private static String getJsonPathNumHashFunctions(String attributeName) {
    return "$..attributeEncoders." + attributeName +
            "." + JsonModifier.classSelector("featureEncoder", "KeyStoreRandomHashing") + ".numHashFunctions";
  }

  private static Set<String> getIncludedAttributes(String encoderConfig, Collection<String> potentialAttributes) {
    Set<String> attributeNames = new HashSet<>();
    for (String potentialAttribute : potentialAttributes) {
      String jsonPath = getJsonPathNumHashFunctions(potentialAttribute);
      if (JsonModifier.test(encoderConfig, jsonPath)) {
        JSONArray arr = (JSONArray) JsonModifier.read(encoderConfig, jsonPath);
        if (!arr.isEmpty()) {
          attributeNames.add(potentialAttribute);
        }
      }
    }
    return attributeNames;
  }

  private static String updateEncoderNumberOfHashFunctions(String encoderConfig, Map<String, Double> numberOfHashFunctionsPerAttribute) {
    int numberOfUpdates = 0;
    String updatedEncoderConfig = encoderConfig;
    for (Map.Entry<String, Double> entry : numberOfHashFunctionsPerAttribute.entrySet()) {
      String attributeName = entry.getKey();
      Double k = entry.getValue();
      String jsonPath = getJsonPathNumHashFunctions(attributeName);
      try {
        updatedEncoderConfig = JsonModifier.set(encoderConfig, jsonPath, (int) Math.round(k));
        if (!updatedEncoderConfig.equals(encoderConfig)) {
          numberOfUpdates++;
          encoderConfig = updatedEncoderConfig;
        }
      } catch (PathNotFoundException exception) {
        log.info("Failed to update numHashFunctions for attribute {} in encoder: {}",
                attributeName, exception.fillInStackTrace().toString());
      }
    }
    log.info("Updated {} attribute numHashFunctions configurations", numberOfUpdates);
    return updatedEncoderConfig;
  }

  private static String updateFrequencyProviderDataset(String encoderConfig, long datasetId) {
    int numberOfUpdates = 0;
    String updatedEncoderConfig = encoderConfig;
    try {
      updatedEncoderConfig = JsonModifier.set(encoderConfig, JSON_FREQUENCY_LOOKUP_PROVIDER_DB_DATASETID, datasetId);
      if (!updatedEncoderConfig.equals(encoderConfig)) {
        numberOfUpdates++;
      }
    } catch (PathNotFoundException exception) {
      log.info("Failed to update frequency provider datasetId in encoder: {}",
              exception.fillInStackTrace().toString());
    }
    log.info("Updated {} frequency provider dataset configurations", numberOfUpdates);
    return updatedEncoderConfig;
  }

  private static String updateFrequencyProviderSource(String encoderConfig, String source) {
    int numberOfUpdates = 0;
    String updatedEncoderConfig = encoderConfig;
    try {
      updatedEncoderConfig = JsonModifier.set(encoderConfig, JSON_FREQUENCY_LOOKUP_PROVIDER_DB_DATASOURCE, source);
      if (!updatedEncoderConfig.equals(encoderConfig)) {
        numberOfUpdates++;
      }
    } catch (PathNotFoundException exception) {
      log.info("Failed to update frequency provider source in encoder: {}",
              exception.fillInStackTrace().toString());
    }
    log.info("Updated {} freqeuency provider source configurations", numberOfUpdates);
    return updatedEncoderConfig;
  }
}

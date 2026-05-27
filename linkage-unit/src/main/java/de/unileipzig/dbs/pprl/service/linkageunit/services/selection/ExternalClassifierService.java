package de.unileipzig.dbs.pprl.service.linkageunit.services.selection;

import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoRecordPair;
import de.unileipzig.dbs.pprl.service.linkageunit.config.ExternalClassifierConnectionConfig;
import de.unileipzig.dbs.pprl.service.linkageunit.services.clients.classifier.api.DefaultApi;
import de.unileipzig.dbs.pprl.service.linkageunit.services.clients.classifier.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.util.*;

import static de.unileipzig.dbs.pprl.service.linkageunit.services.LinkImprovementService.PROPERTY_IMPROVED_LINK;

@Slf4j
@Service
public class ExternalClassifierService {

  private ExternalClassifierConnectionConfig config;

  private DefaultApi api = new DefaultApi();

  public ExternalClassifierService(ExternalClassifierConnectionConfig config) {
    this.config = config;
    this.loadConfig();
  }

  public void loadConfig() {
    log.info("Setting connection parameters from config: {}", this.config);
    api.getApiClient().setBasePath(this.config.getEndpoint());
    api.getApiClient().addDefaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + this.config.getBearerToken());
  }

  public boolean checkConnection() {
    log.info("Checking connection to external selection service...");
    Flux<ModelMetadata> modelMetadataModelsMetadataGet = api.getModelMetadataModelsMetadataGet()
            .doOnError(e -> log.warn("Could not connect to external classification service: {}", e.getMessage()))
            .onErrorResume(e -> Flux.empty()); // Return an empty Flux as fallback
    List<ModelMetadata> modelMetadataList = modelMetadataModelsMetadataGet.collectList().block();
    if (modelMetadataList == null || modelMetadataList.isEmpty()) {
      log.warn("No models found in external service");
    }
    return true;
  }

  public List<MongoRecordPair> runSelection(List<MongoRecordPair> uncertainLinks) {
    StrategyType selectionStrategy = StrategyType.RANDOM;
//    StrategyType selectionStrategy = StrategyType.SKLEARN;
    List<Sample> samples = uncertainLinks.stream()
            .filter(mrp -> {
              if (List.of(StrategyType.RANDOM, StrategyType.UNCERTAINTY).contains(selectionStrategy)) {
                return !mrp.getProperties().contains(PROPERTY_IMPROVED_LINK);
              }
              return true;
            })
            .map(mrp -> {
              Map<String, BigDecimal> features = new HashMap<>();
              Optional<Map<String, Double>> attributeSimilarities = mrp.getAttributeSimilarities();
              if (attributeSimilarities.isPresent()) {
                Map<String, Double> attrSim = attributeSimilarities.get();
                for (String s : attrSim.keySet()) {
                  try {
                    features.put(s, BigDecimal.valueOf(attrSim.get(s)));
                  } catch (NumberFormatException e) {
//                    log.warn("Could not parse as BigDecimal, skipping: {}", attrSim.get(s));
                  }
                }
              } else {
                features.put("Pair", BigDecimal.valueOf(mrp.getSimilarity()));
              }
              return Sample.builder()
                      .id(mrp.getPairId())
                      .features(features)
                      .build();
            })
            .toList();
    log.info("Sending {} samples to external service.", samples.size());
    if (!samples.isEmpty()) {
      log.info("First sample: {}", samples.getFirst());
    }
    SampleSelectionResponse response = api.selectSelectionPost(
            SampleSelectionRequest.builder()
                    .strategyType(selectionStrategy)
                    .strategyOptions(Map.of(
                                    "method", "CoreSet",
                                    "limit", String.valueOf(1000)
                            )
                    )
                    .samples(samples)
                    .build()
    ).block();
    log.info("External selection metadata: {}", response.getMetadata());

    Set<String> selectedIds = new HashSet<>(response.getSelectedIds());
    log.info("External service returned {} selected samples", selectedIds.size());
    List<MongoRecordPair> selectedPairs = uncertainLinks.stream()
            .filter(mrp -> selectedIds.contains(mrp.getPairId()))
            .filter(mrp -> !mrp.getProperties().contains(PROPERTY_IMPROVED_LINK))
            .toList();
    log.info("Selected {} pairs after filtering out previously improved links", selectedPairs.size());
    return selectedPairs;
  }
}

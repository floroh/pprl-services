package de.unileipzig.dbs.pprl.service.protocol.service;

import de.unileipzig.dbs.pprl.service.common.modifier.JsonModifier;
import de.unileipzig.dbs.pprl.service.common.services.DatasetIdService;
import de.unileipzig.dbs.pprl.service.linkageunit.config.LinkSelectionStrategy;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.BatchMatchProjectDto;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.MatchingDto;
import de.unileipzig.dbs.pprl.service.protocol.api.MatcherApi;
import de.unileipzig.dbs.pprl.service.protocol.model.mongo.Layer;
import de.unileipzig.dbs.pprl.service.protocol.model.mongo.MultiLayerProtocol;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.bson.types.ObjectId;

import java.util.LinkedHashMap;

import static de.unileipzig.dbs.pprl.service.common.Constants.DUMMY_LINKAGE_PROJECT;
import static de.unileipzig.dbs.pprl.service.linkageunit.services.LinkImprovementService.*;
import static de.unileipzig.dbs.pprl.service.linkageunit.services.MatcherModificationService.CONFIG_REPLACE_ONLY_CHANGED_PAIRS_ON_RECLASSIFICATION;
import static de.unileipzig.dbs.pprl.service.linkageunit.services.ProjectService.CONFIG_RECORD_PAIR_LIMIT;

@Slf4j
public class MultiLayerProjectCreator {

  private static final String JSONPATH_MINIMAL_SIMILARITY = "$.." +
    JsonModifier.classSelector("recordPairTester", "SimilarityBasedTester") +
    ".minimalSimilarity";
  private static final String JSONPATH_BLOCKER = "$.." +
    JsonModifier.classSelector("blocker", "StandardBlocking") +
    ".blockingKeyExtractors";

  private static final String JSONPATH_THRESHOLD = "$.." +
    JsonModifier.classSelector("classifier", "TrainableThresholdClassifier") +
    ".threshold";
  private static final String JSONPATH_ERROR_RATE = "$.." +
    JsonModifier.fullClassSelector("classifier", "de.unileipzig.dbs.pprl.service.linkageunit.dataset" +
      ".GroundTruthBasedClassifier") +
    ".errorRate";
  private static final String JSONPATH_WEKA_DATASET = "$.." +
    JsonModifier.classSelector("classifier", "WekaClassifier") +
    ".serializedDataset";

  @Getter
  @Setter
  private MultiLayerProtocol protocol;

  private final ProtocolService protocolService;

  private final DatasetIdService datasetIdService;

  public MultiLayerProjectCreator(DatasetIdService datasetIdService, ProtocolService protocolService) {
    this.datasetIdService = datasetIdService;
    this.protocolService = protocolService;
  }

  private MatcherApi getMatcher() {
    return protocolService.getLinkageUnitService().getMatcherApi();
  }

  public MultiLayerProtocol initMultiLayerProtocol(MultiLayerProtocol protocol) {
    if (protocol.getLinkageProject() == null || protocol.getLinkageProject().isBlank()) {
      protocol.setLinkageProject(DUMMY_LINKAGE_PROJECT);
    }
    boolean useRBF = protocol.getLayerByName("RBF").isPresent();
    boolean useABF = protocol.getLayerByName("ABF").isPresent();
    boolean usePPCR = protocol.getLayerByName("PPCR").isPresent();
    int recordPairLimit = 0;
    if (protocol.getLayers().size() > 1) {
      recordPairLimit = 100000;
    }
//    if (!useRBF) {
//      throw new RuntimeException("Missing RBF layer not supported");
//    }
    BatchMatchProjectDto previousProject = null;
    BatchMatchProjectDto rbfProject = null;
    Long initialDatasetId = protocol.getInitialDatasetId();
    if (initialDatasetId == null || initialDatasetId == 0) {
      initialDatasetId = datasetIdService.generateDatasetId();
      protocol.setInitialDatasetId(initialDatasetId);
    }
    if (useRBF) {
      Layer rbfLayer = protocol.getLayerByName("RBF").get();
      rbfLayer.setMatcherMethod(addMatcherCopy(rbfLayer.getMatcherMethod()));
      updateThresholdInConfig(rbfLayer.getMatcherMethod(), rbfLayer.getInitialThreshold());
      String wishMethod = null;
      if (useABF) {
        Layer abfLayer = protocol.getLayerByName("ABF").get();
        wishMethod = abfLayer.getEncodingMethod();
      } else if (usePPCR) {
        Layer ppcrLayer = protocol.getLayerByName("PPCR").get();
        wishMethod = ppcrLayer.getEncodingMethod();
      }
      rbfProject = createProject(
              initialDatasetId,
              rbfLayer.getMatcherMethod(),
              null,
              wishMethod,
              rbfLayer.getLinkSelectionStrategy(),
              true,
              recordPairLimit
      );
      rbfLayer.setProject(rbfProject);
      previousProject = rbfProject;
    }

    BatchMatchProjectDto abfProject = null;
    if (useABF) {
      Layer abfLayer = protocol.getLayerByName("ABF").get();
      String wishMethod = null;
      if (usePPCR) {
        Layer ppcrLayer = protocol.getLayers().getLast();
        wishMethod = ppcrLayer.getEncodingMethod();
      }
      if (useRBF) {
        abfLayer.setMatcherMethod(addAbfSecondRoundMatcher(abfLayer.getMatcherMethod()));
        abfProject = addSubProject(
          rbfProject,
          abfLayer.getMatcherMethod(),
          wishMethod,
          abfLayer.getLinkSelectionStrategy()
        );
      } else {
        abfProject = createProject(
                initialDatasetId,
                abfLayer.getMatcherMethod(),
                null,
                wishMethod,
                abfLayer.getLinkSelectionStrategy(),
                false,
                recordPairLimit
        );
      }
      abfLayer.setProject(abfProject);
      previousProject = abfProject;
    }

    BatchMatchProjectDto ppcrProject = null;
    if (usePPCR) {
      Layer ppcrLayer = protocol.getLayerByName("PPCR").get();
      ppcrLayer.setMatcherMethod(addMatcherCopy(ppcrLayer.getMatcherMethod()));
      updateClassifierErrorRate(ppcrLayer.getMatcherMethod(), ppcrLayer.getErrorRate());

      ppcrProject = addSubProject(
        previousProject,
        ppcrLayer.getMatcherMethod(),
        null, null
      );
      ppcrLayer.setProject(ppcrProject);
    }
    return protocol;
  }

  public BatchMatchProjectDto addSubProject(BatchMatchProjectDto superProjectDto, String method,
    String wishMethod, LinkSelectionStrategy linkSelectionStrategy) {
    long datasetId =
      superProjectDto.getDatasetId() + new ObjectId(superProjectDto.getProjectId()).getTimestamp();
    BatchMatchProjectDto subProjectDto = createProject(datasetId, method, superProjectDto.getProjectId(),
      wishMethod, linkSelectionStrategy, false, 0
    );
    log.debug("Created sub project: " + subProjectDto);
    return subProjectDto;
  }

  public String addAbfSecondRoundMatcher(String baseMethod) {
    MatchingDto baseMatching = getMatcher().fetchMatching(baseMethod);
    String newMethod = baseMethod + "/" + new ObjectId().toHexString();
    baseMatching.getId().setMethod(newMethod);
    String config = baseMatching.getConfig();
    config = JsonModifier.set(config, JSONPATH_MINIMAL_SIMILARITY, 0.0);
    JSONArray ID_BLOCKER_ONLY = new JSONArray();
    LinkedHashMap<String, String> idBlockerConfig = new LinkedHashMap<>();
    idBlockerConfig.put("@class", ".IdBlocker");
    idBlockerConfig.put("id", "blkId");
    idBlockerConfig.put("idName", "BLOCK_ID");
    ID_BLOCKER_ONLY.add(idBlockerConfig);
    config = JsonModifier.set(config, JSONPATH_BLOCKER, ID_BLOCKER_ONLY);
    baseMatching.setConfig(config);
    log.info("Adding new matcher with method: " + newMethod);
    getMatcher().addMatching(baseMatching);
    return newMethod;
  }

  public void updateThresholdInConfig(String baseMethod, double newThreshold) {
    MatchingDto baseMatching = getMatcher().fetchMatching(baseMethod);
    String config = baseMatching.getConfig();
    config = JsonModifier.set(config, JSONPATH_THRESHOLD, newThreshold);
    baseMatching.setConfig(config);
    getMatcher().updateMatching(baseMatching);
  }

  public void updateClassifierErrorRate(String baseMethod, double errorRate) {
    MatchingDto baseMatching = getMatcher().fetchMatching(baseMethod);
    String config = baseMatching.getConfig();
    config = JsonModifier.set(config, JSONPATH_ERROR_RATE, errorRate);
    baseMatching.setConfig(config);
    getMatcher().updateMatching(baseMatching);
  }

  public String addMatcherCopy(String baseMethod) {
    MatchingDto baseMatching = getMatcher().fetchMatching(baseMethod);
    String newMethod = baseMethod + "/" + new ObjectId().toHexString();
    baseMatching.getId().setMethod(newMethod);
    log.info("Adding new matcher with method: " + newMethod);
    getMatcher().addMatching(baseMatching);
    return newMethod;
  }

  public BatchMatchProjectDto createProject(long datasetId, String method, String projectIdToReportTo,
    String wishMethod, LinkSelectionStrategy linkSelectionStrategy,
                                            Boolean replaceOnlyChangedPairsOnReclassification,
                                            int recordPairLimit) {
    log.debug("Creating project...");
    BatchMatchProjectDto.BatchMatchProjectDtoBuilder builder = BatchMatchProjectDto.builder()
      .datasetId(datasetId)
      .method(method)
      .interactive(true);

    if (projectIdToReportTo != null) {
      builder.config(CONFIG_PROJECT_ID_TO_REPORT_TO, projectIdToReportTo);
    }
    if (wishMethod != null) {
      builder.config(CONFIG_WISH_ENCODING_METHOD, wishMethod);
    }
    if (linkSelectionStrategy != null) {
      builder.config(CONFIG_LINK_SELECTION_STRATEGY, linkSelectionStrategy.name());
    }
    if (replaceOnlyChangedPairsOnReclassification != null) {
      builder.config(CONFIG_REPLACE_ONLY_CHANGED_PAIRS_ON_RECLASSIFICATION, replaceOnlyChangedPairsOnReclassification.toString());
    }
    if (recordPairLimit > 0) {
      builder.config(CONFIG_RECORD_PAIR_LIMIT, Integer.toString(recordPairLimit));
    }
    BatchMatchProjectDto project = addProjectDto(builder.build());
    log.info("Created project {}", project);
    return project;
  }

  public BatchMatchProjectDto addProjectDto(BatchMatchProjectDto projectDto) {
    return getMatcher().createProject(projectDto);
  }
}

package de.unileipzig.dbs.pprl.service.linkageunit.services;

import de.unileipzig.dbs.pprl.core.analyzer.linking.SimilarityDistributionAnalyzer;
import de.unileipzig.dbs.pprl.core.analyzer.results.ResultSet;
import de.unileipzig.dbs.pprl.core.common.TableSerialization;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import de.unileipzig.dbs.pprl.core.common.monitoring.Tag;
import de.unileipzig.dbs.pprl.core.matcher.MatcherSerialization;
import de.unileipzig.dbs.pprl.core.matcher.MatcherUtils;
import de.unileipzig.dbs.pprl.core.matcher.classification.Classifier;
import de.unileipzig.dbs.pprl.core.matcher.classification.MultiThresholdClassifier;
import de.unileipzig.dbs.pprl.core.matcher.classification.TrainableThresholdClassifier;
import de.unileipzig.dbs.pprl.core.matcher.linking.DefaultLinker;
import de.unileipzig.dbs.pprl.core.matcher.matcher.DatasetBasedBatchMatcher;
import de.unileipzig.dbs.pprl.core.matcher.matcher.Matcher;
import de.unileipzig.dbs.pprl.core.matcher.model.api.LinkageProcessDataSet;
import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoGroundTruth;
import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoRecordPair;
import de.unileipzig.dbs.pprl.service.common.services.DatasetMongoService;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.BatchMatchProjectDto;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.MatcherIdDto;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.MatcherTrainingsRequest;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.MatcherUpdateRequest;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.MatcherUpdateType;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.MatchingDto;
import de.unileipzig.dbs.pprl.service.linkageunit.data.mongo.BatchMatchProject;
import de.unileipzig.dbs.pprl.service.linkageunit.data.mongo.ProjectState;
import de.unileipzig.dbs.pprl.service.linkageunit.services.helper.MongoRecordUtils;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static de.unileipzig.dbs.pprl.service.linkageunit.services.LinkImprovementService.PROPERTY_LINK_FROM_UPPER_LAYER;

@Service
@Slf4j
public class MatcherModificationService {

  public static final String PROPERTY_METHOD_PPCR = "METHOD_PPCR";
  private final MatcherProviderService matcherProviderService;

  private final AsynchronousBatchMatcherService matcherService;

  private final ProjectService projectService;
  private final DatasetMongoService datasetService;

  public MatcherModificationService(MatcherProviderService matcherProviderService,
    AsynchronousBatchMatcherService matcherService, ProjectService projectService,
    DatasetMongoService datasetService) {
    this.matcherProviderService = matcherProviderService;
    this.matcherService = matcherService;
    this.projectService = projectService;
    this.datasetService = datasetService;
  }

  /**
   * Update matcher based on NEW improved links.
   *
   * @param request Matcher update request
   * @return updated matcher
   */
  public MatchingDto update(MatcherUpdateRequest request) {
    ObjectId projectId = new ObjectId(request.getProjectId());
    log.info("Updating matcher of project {} with strategy {}.", request.getProjectId(),
      request.getType().toString());
    if (request.getType() == MatcherUpdateType.NEW_IMPROVED) {
      return updateWithNewImproved(projectId);
    } else if (request.getType() == MatcherUpdateType.UPPER_IMPROVED) {
      return fitWithUpperAndImprovedLinks(projectId);
    } else if (request.getType() == MatcherUpdateType.IMPROVED) {
      return fitWithImprovedLinks(projectId);
    } else if (request.getType() == MatcherUpdateType.CR_ONLY) {
      return fitWithReviewedLinksOnly(projectId);
    } else {
      throw new RuntimeException("Not implemented yet");
    }
  }

  private MatchingDto updateWithNewImproved(ObjectId projectId) {
    List<RecordPair> improvedRecordPairs = projectService.getRecordPairsFilteredByProperties(
      projectId, Set.of(LinkImprovementService.PROPERTY_IMPROVED_LINK)
    );
    int improvedCount = improvedRecordPairs.size();
    Collection<RecordPair> newPairs = improvedRecordPairs.stream()
      .map(rp -> (MongoRecordPair) rp)
      .filter(mrp -> mrp.getProperties().contains(LinkageProcessDataSet.NEW))
      .peek(mrp -> mrp.getProperties().remove(LinkageProcessDataSet.NEW))
      .peek(MatcherUtils::addLabelBasedOnMatchGrade)
      .collect(Collectors.toList());
    log.info("Updating matcher based on {} new of {} improved links.", newPairs.size(), improvedCount);
    MatchingDto matchingDto = matcherProviderService.getMatchingById(MatcherIdDto.builder()
      .method(projectService.getProject(projectId).getMethod())
      .build()).orElseThrow();

    MatchingDto updatedMatching = update(matchingDto, newPairs);
    newPairs = MongoRecordUtils.removeLabelTag(newPairs);
    projectService.addRecordPairs(projectId, newPairs.stream().map(rp -> (MongoRecordPair) rp).toList());
    return updatedMatching;
  }

  /**
   * Update matcher based on a collection of labeled data.
   */
  public MatchingDto update(MatchingDto matchingDto, Collection<RecordPair> labeledData) {
    log.info("Updating matcher {} based on {} labeled instances.", matchingDto.getId(), labeledData.size());
    MatcherIdDto matcherId = matchingDto.getId();
    Matcher updatedMatcher =
      matcherService.trainMatcher(matcherProviderService.getMatcher(matcherId), true, labeledData);
    matchingDto.setConfig(MatcherSerialization.serializeJson(updatedMatcher));
    matcherProviderService.update(matchingDto);
    return matchingDto;
  }

  private MatchingDto fitWithUpperAndImprovedLinks(ObjectId projectId) {
    return fit(projectId, Set.of(
      LinkageProcessDataSet.REPLACED, PROPERTY_LINK_FROM_UPPER_LAYER,
      LinkImprovementService.PROPERTY_IMPROVED_LINK
    ));
  }

  private MatchingDto fitWithImprovedLinks(ObjectId projectId) {
    return fit(projectId, Set.of(LinkImprovementService.PROPERTY_IMPROVED_LINK));
  }
  private MatchingDto fitWithReviewedLinksOnly(ObjectId projectId) {
    return fit(projectId, Set.of(LinkImprovementService.PROPERTY_IMPROVED_LINK, PROPERTY_METHOD_PPCR));
  }

  private MatchingDto fit(ObjectId projectId, Set<String> properties) {
    properties = new HashSet<>(properties);
    MatchingDto matchingDto = matcherProviderService.getMatchingById(MatcherIdDto.builder()
      .method(projectService.getProject(projectId).getMethod())
      .build()).orElseThrow();
    List<RecordPair> allRecordPairs;
    if (properties.contains(LinkageProcessDataSet.REPLACED)) {
      allRecordPairs = projectService.getAllRecordPairs(projectId);
    } else {
      allRecordPairs = projectService.getRecordPairs(projectId);
    }
    List<RecordPair> pairsForTraining = allRecordPairs;
    if (properties.contains(PROPERTY_METHOD_PPCR)) {
      pairsForTraining = pairsForTraining.stream()
        .filter(rp -> rp.getTags().stream()
          .filter(tag -> tag.getTag().equals(LinkImprovementService.TAG_ENCODING_METHOD))
          .anyMatch(tag -> tag.getStringValue().contains("CR"))
        ).collect(Collectors.toList());
      properties.remove(PROPERTY_METHOD_PPCR);
    }
    final Set<String> finalProperties = properties;
    Collection<MongoRecordPair> recordPairs = pairsForTraining.stream()
      .map(rp -> (MongoRecordPair) rp)
      .filter(mrp -> {
        for (String property : finalProperties) {
          if (mrp.getProperties().contains(property)) {
            return true;
          }
        }
        return false;
      })
      .peek(mrp -> mrp.getProperties().remove(LinkageProcessDataSet.NEW))
      .peek(MatcherUtils::addLabelBasedOnMatchGrade)
      .collect(Collectors.toList());
    recordPairs = MongoRecordUtils.removePairsWhereImprovedLinkIsAvailable(recordPairs);
    log.info("Updating matcher based on {} links.", recordPairs.size());
    MatcherIdDto matcherId = matchingDto.getId();
    Matcher matcher = matcherProviderService.getMatcher(matcherId);
    provideSimilarityDistributionToTrainableThresholdClassifier(matcher, allRecordPairs);
    Matcher updatedMatcher =
      matcherService.trainMatcher(matcher, false, new ArrayList<>(recordPairs));
    matchingDto.setConfig(MatcherSerialization.serializeJson(updatedMatcher));
    matcherProviderService.update(matchingDto);

    // Remove label tags and persist record pairs without new property
    recordPairs = MongoRecordUtils.removeLabelTag(recordPairs);
    projectService.addRecordPairs(projectId, recordPairs);
    return matchingDto;
  }

  private void modifyTrainableThresholdClassifier(Matcher matcher) {
    if (matcher instanceof DatasetBasedBatchMatcher) {
      DefaultLinker linker = (DefaultLinker) ((DatasetBasedBatchMatcher) matcher).getLinker();
      Classifier classifier = linker.getClassifier();
      if (classifier instanceof TrainableThresholdClassifier) {
        TrainableThresholdClassifier trainableThresholdClassifier =
          (TrainableThresholdClassifier) classifier;

      }
    }
  }

  private void provideSimilarityDistributionToTrainableThresholdClassifier(Matcher matcher,
    Collection<RecordPair> recordPairs) {
    recordPairs = recordPairs.stream()
      .filter(rp -> !((MongoRecordPair) rp).getProperties().contains(LinkageProcessDataSet.REPLACED))
      .collect(Collectors.toList());
    if (matcher instanceof DatasetBasedBatchMatcher) {
      DefaultLinker linker = (DefaultLinker) ((DatasetBasedBatchMatcher) matcher).getLinker();
      Classifier classifier = linker.getClassifier();
      if (classifier instanceof TrainableThresholdClassifier) {
        TrainableThresholdClassifier trainableThresholdClassifier =
          (TrainableThresholdClassifier) classifier;
        if (trainableThresholdClassifier.getSimilarityDistribution() != null) {
          log.warn("Similarity distribution already provided");
          return;
        }
        log.warn("Providing similarity distribution to trainable threshold classifier");
        SimilarityDistributionAnalyzer analyzer = new SimilarityDistributionAnalyzer();
        analyzer.setBinSize(0.01);
        ResultSet resultSet = analyzer.analyze(recordPairs);
        trainableThresholdClassifier.setSimilarityDistribution(
          TableSerialization.toDefaultSerializableTable(resultSet.getAsTable())
        );
      }
    }
  }

  public void reclassify(ObjectId projectId) {
    List<RecordPair> recordPairs = projectService.getRecordPairs(projectId);
    long paircount = recordPairs.size();
    recordPairs = recordPairs.stream()
      .map(rp -> (MongoRecordPair) rp)
      .filter(mrp -> !mrp.getProperties().contains(LinkImprovementService.PROPERTY_IMPROVED_LINK))
      .peek(mrp -> {
        mrp.addProperty(LinkageProcessDataSet.ACTIVE);
        mrp.removeProperty(LinkageProcessDataSet.NEW);
        mrp.getTags().remove(Tag.create(LinkageProcessDataSet.TAG_REMOVED_BY_CLASSIFIER));
        mrp.getTags().remove(Tag.create(LinkageProcessDataSet.TAG_REMOVED_BY_POSTPROCESSING));
//        mrp.getTags().remove(Tag.create(Classifier.Label.TRUE_MATCH.name()));
//        mrp.getTags().remove(Tag.create(Classifier.Label.TRUE_NON_MATCH.name()));
      })
      .collect(Collectors.toList());
    long withOutImprovedLinks = recordPairs.size();
    log.info("All: {}, Improved: {}", paircount, paircount - withOutImprovedLinks);

    DatasetBasedBatchMatcher matcher = matcherService.getMatcher(projectService.getProject(projectId));

    log.info("Reclassifying {} pairs with classifier: {}", recordPairs.size(),
      ((DefaultLinker) matcher.getLinker()).getClassifier()
    );
    matcher.reclassifyRecordPairs(recordPairs);
  }

  public MatchingDto trainWithGroundTruth(MatcherTrainingsRequest request) {
    MatcherIdDto inputId = request.getMatcherId();
    int trainingsDatasetId = request.getDatasetId();
    double minSimilarity = request.getMinSimilarity();
    double maxSimilarity = request.getMaxSimilarity() == 0 ? 1.0 : request.getMaxSimilarity();

    Matcher matcher = matcherProviderService.getMatcher(inputId);
    MongoGroundTruth mongoGroundTruth =
      datasetService.getGroundTruth(trainingsDatasetId).orElseThrow(() ->
        new RuntimeException("No ground truth found for dataset " + trainingsDatasetId));

    // Create dummy matcher config with a simple classifier to get features of record pairs
    DatasetBasedBatchMatcher datasetBasedBatchMatcher =
      (DatasetBasedBatchMatcher) matcherProviderService.getMatcher(inputId);
//    ((StandardBlocking) datasetBasedBatchMatcher.getBlocker())
//      .addBlockingKeyExtractor(new IdBlocker("idBlocker", "BLOCK_ID"));
    ((DefaultLinker) datasetBasedBatchMatcher.getLinker()).setClassifier(
      new MultiThresholdClassifier(0.65, 0.85, 0.95)
    );

    MatcherIdDto newMatcherId = MatcherIdDto.builder()
      .method(inputId.getMethod() + "/trained/" + trainingsDatasetId + "/" + new ObjectId().toHexString())
      .build();
    MatchingDto matchingDto = MatchingDto.builder()
      .config(MatcherSerialization.serializeJson(datasetBasedBatchMatcher))
      .id(newMatcherId)
      .build();
    matcherProviderService.add(matchingDto);
    log.debug("Created dummy matcher config");

    // Create dummy project
    BatchMatchProject tmpMatchProject = projectService.add(BatchMatchProjectDto.builder()
      .datasetId(trainingsDatasetId)
      .method(newMatcherId.getMethod())
      .interactive(true)
      .build());
    ObjectId tmpMatchProjectProjectId = tmpMatchProject.getProjectId();
    log.debug("Created dummy project {} for training", tmpMatchProjectProjectId);

    // Run linkage in dummy project with matcher
    matcherService.runTo(
      tmpMatchProjectProjectId,
      ProjectState.CLASSIFICATION
    );

    // Fetch and label resulting record pairs
    log.debug("Fetching and labeling resulting record pairs");
    Set<String> trueMatchPairIds = mongoGroundTruth.getTrueMatchPairIds();
    List<RecordPair> labeledTrainingsData = projectService.getRecordPairs(tmpMatchProjectProjectId).stream()
      .map(rp -> {
        boolean isMatch = trueMatchPairIds.contains(rp.getPairId());
        RecordPair newRp = rp.duplicate();
        newRp.addTag((isMatch ? Classifier.Label.TRUE_MATCH :
          Classifier.Label.TRUE_NON_MATCH).toString());
        return newRp;
      })
      .filter(rp -> rp.getSimilarity() > minSimilarity && rp.getSimilarity() < maxSimilarity)
      .collect(Collectors.toList());

    // Train matcher
    log.debug("Training matcher");
    Matcher newMatcher = matcherService.trainMatcher(matcher, false, labeledTrainingsData);

    // Persist new matcher
    matchingDto.setConfig(MatcherSerialization.serializeJson(newMatcher));
    matcherProviderService.update(matchingDto);
    log.info("Persisted trained matcher: {}", matchingDto.getId());

    projectService.deleteProject(tmpMatchProjectProjectId);
    return matchingDto;
  }
}

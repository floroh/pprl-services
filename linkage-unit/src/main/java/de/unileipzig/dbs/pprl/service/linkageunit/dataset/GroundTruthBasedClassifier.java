package de.unileipzig.dbs.pprl.service.linkageunit.dataset;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import de.unileipzig.dbs.pprl.core.common.model.impl.MatchGrade;
import de.unileipzig.dbs.pprl.core.matcher.classification.Classifier;
import de.unileipzig.dbs.pprl.service.common.SpringContext;
import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoGroundTruth;
import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoRecordPair;
import de.unileipzig.dbs.pprl.service.common.persistence.repositories.mongo.MongoGroundTruthRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

@Slf4j
public class GroundTruthBasedClassifier implements Classifier {

  private double errorRate = 0;

  @JsonIgnore
  private final MongoGroundTruthRepository groundTruthRepository;

  @JsonIgnore
  private final Map<Integer, Set<String>> expectedPairIdsByDatasetId = new HashMap<>();

  @JsonIgnore
  private final Random random = new Random();

  public GroundTruthBasedClassifier(double errorRate) {
    this();
    this.errorRate = errorRate;
  }

  private GroundTruthBasedClassifier() {
    this.groundTruthRepository = SpringContext.getBean(MongoGroundTruthRepository.class);
  }

  @Override
  public MatchGrade classify(RecordPair recordPairWithSimilarity) {
    int idDataset = ((MongoRecordPair) recordPairWithSimilarity).getLeftRecord().getIdDataset();
    Set<String> expectedPairIds = getOrInitExpectedPairIds(idDataset);

    String pairId = recordPairWithSimilarity.getPairId();
    MatchGrade matchGrade;
    if (expectedPairIds.contains(pairId)) {
      matchGrade = MatchGrade.CERTAIN_MATCH;
    } else {
      matchGrade = MatchGrade.NON_MATCH;
    }
    matchGrade = flipBasedOnErrorRate(matchGrade);
    addProbabilityTag(recordPairWithSimilarity, 1.0);
    return matchGrade;
  }

  private Set<String> getOrInitExpectedPairIds(int idDataset) {
    Set<String> expectedPairIds = expectedPairIdsByDatasetId.get(idDataset);
    if (expectedPairIds == null) {
      Optional<MongoGroundTruth> byDatasetId = groundTruthRepository.findByDatasetId(idDataset);
      if (byDatasetId.isEmpty()) {
        throw new IllegalStateException("No ground truth for dataset with id " + idDataset + " found.");
      }
      expectedPairIds = byDatasetId.get().getTrueMatchPairIds();
      log.debug("Build new expected pair ids set for dataset {} with {} entries.", idDataset,
        expectedPairIds.size());
      expectedPairIdsByDatasetId.put(idDataset, expectedPairIds);
    }
    return expectedPairIds;
  }

  private MatchGrade flipBasedOnErrorRate(MatchGrade matchGrade) {
    if (random.nextDouble(1.0) < errorRate) {
      if (matchGrade == MatchGrade.CERTAIN_MATCH) {
        matchGrade = MatchGrade.NON_MATCH;
      } else {
        matchGrade = MatchGrade.CERTAIN_MATCH;
      }
    }
    return matchGrade;
  }

  public double getErrorRate() {
    return errorRate;
  }


  @Override
  public String toString() {
    return "GroundTruthBasedClassifier{" +
      "errorRate=" + errorRate +
      '}';
  }
}

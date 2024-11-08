package de.unileipzig.dbs.pprl.core.matcher.similarityCalculation.record;

import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;

public class DummyRecordSimilarityCalculator implements RecordSimilarityCalculator {

  @Override
  public double calculateSimilarity(RecordPair recordPair) {
    return 1.0;
  }
}

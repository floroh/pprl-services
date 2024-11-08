package de.unileipzig.dbs.pprl.core.matcher.linking;

import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;

public class SimilarityBasedTester implements RecordPairTester {

  private double minimalSimilarity;

  public SimilarityBasedTester(double minimalSimilarity) {
    this.minimalSimilarity = minimalSimilarity;
  }

  private SimilarityBasedTester() {
  }

  @Override
  public boolean test(RecordPair recordPair) {
    return recordPair.getSimilarity() >= minimalSimilarity;
  }

  public double getMinimalSimilarity() {
    return minimalSimilarity;
  }

  public void setMinimalSimilarity(double minimalSimilarity) {
    this.minimalSimilarity = minimalSimilarity;
  }
}

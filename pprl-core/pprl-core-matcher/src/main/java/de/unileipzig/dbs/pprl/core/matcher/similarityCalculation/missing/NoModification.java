package de.unileipzig.dbs.pprl.core.matcher.similarityCalculation.missing;

/**
 * Keep the similarity value as it is
 */
public class NoModification implements MissingSimilarityStrategy {
  @Override
  public double modify(Double similarity, String attributeName) {
    return similarity;
  }
}

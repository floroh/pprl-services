package de.unileipzig.dbs.pprl.core.matcher.similarityCalculation.missing;

/**
 * Replace missing similarity score with a fixed value, e.g. 0.0 or 1.0
 */
public class FixedReplacement implements MissingSimilarityStrategy {

  public static final FixedReplacement ZERO = new FixedReplacement(0.0);
  public static final FixedReplacement ONE = new FixedReplacement(1.0);

  private double replacement;

  public FixedReplacement(double replacement) {
    this.replacement = replacement;
  }

  private FixedReplacement() {
  }

  @Override
  public double modify(Double similarity, String attributeName) {
    if (similarity.equals(MissingSimilarityStrategy.MISSING_SIMILARITY)) {
      return replacement;
    }
    return similarity;
  }

  public double getReplacement() {
    return replacement;
  }
}

package de.unileipzig.dbs.pprl.core.matcher.similarityCalculation.missing;

import de.unileipzig.dbs.pprl.core.matcher.similarityCalculation.aggregation.SimilarityAggregator;

import java.util.Collection;

/**
 * Modify missing values using a {@link MissingSimilarityStrategy}
 * ONLY for a given collection of attributes
 * For other attributes the similarity value is not changed.
 * Intended use:
 * If crucial attributes are missing, their similarity score should be set to 0.
 * If optional attributes are missing, they can be ignored in the {@link SimilarityAggregator}.
 */
public class AttributeDependentReplacement implements MissingSimilarityStrategy {

  public static final FixedReplacement DEFAULT_STRATEGY = FixedReplacement.ZERO;

  private Collection<String> affectedAttributes;

  private MissingSimilarityStrategy strategy;

  public AttributeDependentReplacement(Collection<String> affectedAttributes) {
    this(affectedAttributes, DEFAULT_STRATEGY);
  }

  public AttributeDependentReplacement(Collection<String> affectedAttributes,
    MissingSimilarityStrategy strategy) {
    this.affectedAttributes = affectedAttributes;
    this.strategy = strategy;
  }

  private AttributeDependentReplacement() {
  }

  @Override
  public double modify(Double similarity, String attributeName) {
    return affectedAttributes.contains(attributeName)
      ? strategy.modify(similarity, attributeName)
      : similarity;
  }

  public Collection<String> getAffectedAttributes() {
    return affectedAttributes;
  }

  public MissingSimilarityStrategy getStrategy() {
    return strategy;
  }
}

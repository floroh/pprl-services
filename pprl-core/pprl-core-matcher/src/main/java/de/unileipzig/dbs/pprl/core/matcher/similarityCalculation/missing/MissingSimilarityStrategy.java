package de.unileipzig.dbs.pprl.core.matcher.similarityCalculation.missing;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.unileipzig.dbs.pprl.core.matcher.model.AttributePairWithSimilarity;
import de.unileipzig.dbs.pprl.core.matcher.similarityCalculation.attribute.AttributeSimilarityCalculator;

/**
 * Handle missing similarity values between similarity calculation and aggregation
 * - {@link AttributeSimilarityCalculator} returns the similarity placeholder defined by
 * {@link #MISSING_SIMILARITY}
 * - the strategy here can replace that placeholder
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property = "@class")
public interface MissingSimilarityStrategy {

  double MISSING_SIMILARITY = Double.NaN;

  double modify(Double similarity, String attributeName);

  default AttributePairWithSimilarity modify(AttributePairWithSimilarity ap) {
    ap.setSimilarity(modify(ap.getSimilarity(), ap.getName()));
    return ap;
  }
}

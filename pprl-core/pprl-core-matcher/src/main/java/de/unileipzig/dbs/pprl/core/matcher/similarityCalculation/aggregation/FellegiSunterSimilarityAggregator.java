/*
 * Copyright © 2018 - 2021 Leipzig University (Database Research Group)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.unileipzig.dbs.pprl.core.matcher.similarityCalculation.aggregation;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import de.unileipzig.dbs.pprl.core.matcher.model.AttributePairWithSimilarity;
import de.unileipzig.dbs.pprl.core.matcher.similarityCalculation.missing.MissingSimilarityStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Calculate the aggregate of multiple similarity scores based on agreement and disagreement weights from FS
 * Missing values are ignored
 */
public class FellegiSunterSimilarityAggregator implements SimilarityAggregator {

  @JsonPropertyOrder(alphabetic=true)
  private Map<String, Double> mWeights;
  private Map<String, Double> uWeights;

  private final static Logger logger = LogManager.getLogger(FellegiSunterSimilarityAggregator.class);

  public FellegiSunterSimilarityAggregator() {
    this.mWeights = new HashMap<>();
    this.uWeights = new HashMap<>();
  }

  public FellegiSunterSimilarityAggregator(Map<String, Double> mWeights, Map<String, Double> uWeights) {
    this.mWeights = new HashMap<>(mWeights);
    this.uWeights = new HashMap<>(uWeights);
  }

  /**
   * Aggregate similarities using Fellegi–Sunter style weighted averaging.
   * The final score is normalized between 0 and 1 based on the min/max possible totals.
   */
  @Override
  public Double aggregate(Collection<AttributePairWithSimilarity> pairs) {
    double score = 0.0;
    double minScore = 0.0;
    double maxScore = 0.0;

    for (AttributePairWithSimilarity pair : pairs) {
      Double similarity = pair.getSimilarity();
      if (similarity.equals(MissingSimilarityStrategy.MISSING_SIMILARITY)) {
        continue;
      }

      String attr = pair.getName();
      double m = mWeights.getOrDefault(attr, getAverageWeight(mWeights));
      double u = uWeights.getOrDefault(attr, getAverageWeight(uWeights));

      // Interpolate linearly between disagreement (u) and agreement (m)
      double combined = u + similarity * (m - u);
      score += combined;

      // Compute boundaries for normalization
      maxScore += m;
      minScore += u;
    }

    // Normalize the aggregated score between 0 and 1
    if (maxScore == minScore) {
      return 0.5; // avoid division by zero
    }

    double normalized = (score - minScore) / (maxScore - minScore);
    return Math.min(1.0, Math.max(0.0, normalized));
  }

  private double getAverageWeight(Map<String, Double> weights) {
    return weights.values().stream().mapToDouble(Double::doubleValue).average().orElse(1.0);
  }

  // --- Getters/Setters ---
  public Map<String, Double> getmWeights() {
    return mWeights;
  }

  public void setmWeights(Map<String, Double> mWeights) {
    this.mWeights = mWeights;
  }

  public Map<String, Double> getuWeights() {
    return uWeights;
  }

  public void setuWeights(Map<String, Double> uWeights) {
    this.uWeights = uWeights;
  }

}

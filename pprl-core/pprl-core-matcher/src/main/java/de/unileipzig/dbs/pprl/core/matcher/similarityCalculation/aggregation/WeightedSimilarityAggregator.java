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
import java.util.Optional;

/**
 * Calculate the weighted aggregate of multiple similarity scores
 * Missing values are ignored
 */
public class WeightedSimilarityAggregator implements SimilarityAggregator {

  @JsonPropertyOrder(alphabetic=true)
  private Map<String, Double> weights;

  private final static Logger logger = LogManager.getLogger(WeightedSimilarityAggregator.class);

  public WeightedSimilarityAggregator() {
    this.weights = new HashMap<>();
  }

  public WeightedSimilarityAggregator(Map<String, Double> weights) {
    this.weights = new HashMap<>(weights);
  }

  @Override
  public Double aggregate(Collection<AttributePairWithSimilarity> pairs) {
    double nom = 0.0;
    double div = 0.0;
    for (AttributePairWithSimilarity pair : pairs) {
      Double similarity = pair.getSimilarity();
      if (similarity.equals(MissingSimilarityStrategy.MISSING_SIMILARITY)) {
        continue;
      }
      final String attrName = pair.getName();
      final double w = getWeight(attrName);
      nom += w * similarity;
      div += w;
    }
    return nom / div;
  }

  private Double getWeight(String attrName) {
    Optional<Double> weightIfExists = getWeightIfExists(attrName);
    return weightIfExists.orElseGet(() -> {
      logger.debug("WeightedAggregator uses average weight, because it is missing: " + attrName);
      return getAverageWeight();
    });
  }

  private double getAverageWeight() {
    return weights.values().stream().mapToDouble(d -> d).average().orElse(1);
  }

  public Optional<Double> getWeightIfExists(String name) {
    return Optional.ofNullable(weights.get(name));
  }

  public void setWeight(String name, Double weight) {
    weights.put(name, weight);
  }

  public Map<String, Double> getWeights() {
    return weights;
  }

  public void setWeights(Map<String, Double> weights) {
    this.weights = weights;
  }
}

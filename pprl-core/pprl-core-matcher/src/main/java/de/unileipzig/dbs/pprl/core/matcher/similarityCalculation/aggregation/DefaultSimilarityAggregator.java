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

import de.unileipzig.dbs.pprl.core.matcher.model.AttributePairWithSimilarity;
import de.unileipzig.dbs.pprl.core.matcher.similarityCalculation.missing.MissingSimilarityStrategy;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * Basic aggregator for multiple similarity scores to a single value
 * Supported methods: MIN, MAX, AVERAGE
 * Missing values are ignored
 */
public class DefaultSimilarityAggregator implements SimilarityAggregator {
  public enum AggregationMethod {MIN, MAX, AVERAGE}

  private AggregationMethod aggregationMethod = AggregationMethod.AVERAGE;

  public DefaultSimilarityAggregator() {
  }

  public DefaultSimilarityAggregator(AggregationMethod aggregationMethod) {
    this.aggregationMethod = aggregationMethod;
  }

  @Override
  public Double aggregate(Collection<AttributePairWithSimilarity> candidatePairWithSimilarities) {
    Stream<Double> similarities =
      candidatePairWithSimilarities.stream()
        .map(AttributePairWithSimilarity::getSimilarity)
        .filter(sim -> !sim.equals(MissingSimilarityStrategy.MISSING_SIMILARITY));
    switch (aggregationMethod) {
      case MIN:
        return similarities.min(Double::compareTo).orElse(0d);
      case MAX:
        return similarities.max(Double::compareTo).orElse(0d);
      case AVERAGE:
        return similarities.mapToDouble(d -> d).average().orElse(0d);
    }
    return 0d;
  }

  public AggregationMethod getAggregationMethod() {
    return aggregationMethod;
  }

  public void setAggregationMethod(AggregationMethod aggregationMethod) {
    this.aggregationMethod = aggregationMethod;
  }
}
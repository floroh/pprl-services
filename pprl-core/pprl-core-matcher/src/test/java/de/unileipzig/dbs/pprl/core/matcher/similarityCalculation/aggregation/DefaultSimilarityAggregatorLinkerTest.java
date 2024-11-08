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

import de.unileipzig.dbs.pprl.core.common.factories.AttributeFactory;
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.matcher.MatcherTestBase;
import de.unileipzig.dbs.pprl.core.matcher.model.AttributePairWithSimilarity;
import de.unileipzig.dbs.pprl.core.matcher.similarityCalculation.missing.MissingSimilarityStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultSimilarityAggregatorLinkerTest extends MatcherTestBase {
  private Collection<AttributePairWithSimilarity> attributePairWithSimilarities;

  @BeforeEach
  void setUp() {
    attributePairWithSimilarities = Stream.of(0.6, 0.4, 0.4, 0.8, 1.0, 1.0).map(
      sim -> new AttributePairWithSimilarity("DUMMY", getAttr(getRandomBitVector(32)),
        getAttr(getRandomBitVector(32)), sim
      )).collect(Collectors.toSet());
    attributePairWithSimilarities.add(
      new AttributePairWithSimilarity("DUMMY", getAttr(getRandomBitVector(32)),
        getAttr(getRandomBitVector(32)), MissingSimilarityStrategy.MISSING_SIMILARITY
      )
    );
  }

  @Test
  void aggregateMin() {
    DefaultSimilarityAggregator similarityAggregator =
      new DefaultSimilarityAggregator(DefaultSimilarityAggregator.AggregationMethod.MIN);
    double result = similarityAggregator.aggregate(attributePairWithSimilarities);
    assertEquals(0.4, result, 0.00001);
  }

  @Test
  void aggregateMax() {
    DefaultSimilarityAggregator similarityAggregator =
      new DefaultSimilarityAggregator(DefaultSimilarityAggregator.AggregationMethod.MAX);
    double result = similarityAggregator.aggregate(attributePairWithSimilarities);
    assertEquals(1.0, result, 0.00001);
  }

  @Test
  void aggregateAverage() {
    DefaultSimilarityAggregator similarityAggregator =
      new DefaultSimilarityAggregator(DefaultSimilarityAggregator.AggregationMethod.AVERAGE);
    double result = similarityAggregator.aggregate(attributePairWithSimilarities);
    assertEquals(0.7, result, 0.00001);
  }

  private Attribute getAttr(Object obj) {
    return AttributeFactory.getAttribute(obj);
  }
}
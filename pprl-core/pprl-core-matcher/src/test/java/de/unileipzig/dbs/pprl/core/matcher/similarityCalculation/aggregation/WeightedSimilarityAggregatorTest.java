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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WeightedSimilarityAggregatorTest extends MatcherTestBase {

  private List<AttributePairWithSimilarity> attributePairWithSimilarities;
  private Map<String, Double> weightMap;

  @BeforeEach
  void setUp() {
    attributePairWithSimilarities = new ArrayList<>();
    weightMap = new HashMap<>();
    List<Double> sims = Arrays.asList(0.6, 0.4, 0.4, 0.8, 1.0, 1.0);
    List<Double> weights = Arrays.asList(1.0, 1.0, 2.0, 1.0, 0.5, 0.5);
    for (int i = 0; i < sims.size(); i++) {
      attributePairWithSimilarities.add(
        new AttributePairWithSimilarity("DUMMY_" + i, getAttr(getRandomBitVector(32)),
          getAttr(getRandomBitVector(32)), sims.get(i)
        ));
      weightMap.put("DUMMY_" + i, weights.get(i));
    }
  }

  @Test
  void aggregate() {
    WeightedSimilarityAggregator similarityAggregator = new WeightedSimilarityAggregator(weightMap);
    double result = similarityAggregator.aggregate(attributePairWithSimilarities);
    assertEquals(0.6, result, 0.00001);
  }

  @Test
  void aggregateWithUnknownAttributeName() {
    Map<String, Double> weightMap = Map.of("FIRSTNAME", 2.0, "LASTNAME", 3.0);
    List<AttributePairWithSimilarity> sims = List.of(
      new AttributePairWithSimilarity("FIRSTNAME", getAttr(getRandomBitVector(32)),
        getAttr(getRandomBitVector(32)), 0.6
      ),
      new AttributePairWithSimilarity("LASTNAME", getAttr(getRandomBitVector(32)),
        getAttr(getRandomBitVector(32)), 0.8
      ),
      new AttributePairWithSimilarity("UNKNOWN", getAttr(getRandomBitVector(32)),
        getAttr(getRandomBitVector(32)), 0.9
      )
    );
    WeightedSimilarityAggregator similarityAggregator = new WeightedSimilarityAggregator(weightMap);
    assertTrue(similarityAggregator.getWeightIfExists("UNKNOWN").isEmpty());
    assertDoesNotThrow(() -> similarityAggregator.aggregate(sims));
    assertEquals(0.78, similarityAggregator.aggregate(sims), 0.001);
  }

  @Test
  void aggregateWithZeroWeightAttribute() {
    Map<String, Double> weightMap = Map.of(
      "FIRSTNAME", 2.0,
      "LASTNAME", 3.0,
      "DATEOFBIRTH", 0.0);
    List<AttributePairWithSimilarity> sims = List.of(
      new AttributePairWithSimilarity("FIRSTNAME", getAttr(getRandomBitVector(32)),
        getAttr(getRandomBitVector(32)), 0.6
      ),
      new AttributePairWithSimilarity("LASTNAME", getAttr(getRandomBitVector(32)),
        getAttr(getRandomBitVector(32)), 0.8
      ),
      new AttributePairWithSimilarity("DATEOFBIRTH", getAttr(getRandomBitVector(32)),
        getAttr(getRandomBitVector(32)), 0.9
      )
    );
    WeightedSimilarityAggregator similarityAggregator = new WeightedSimilarityAggregator(weightMap);
    assertEquals(0, similarityAggregator.getWeightIfExists("DATEOFBIRTH").get());
    assertEquals(0.72, similarityAggregator.aggregate(sims), 0.001);
  }

  private Attribute getAttr(Object obj) {
    return AttributeFactory.getAttribute(obj);
  }
}
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

package de.unileipzig.dbs.pprl.core.matcher.similarityCalculation.attribute;

import de.unileipzig.dbs.pprl.core.common.factories.AttributeFactory;
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.matcher.model.AttributePair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StringSimilarityCalculatorTest {

  private AttributePair attributePairSame;
  private AttributePair attributePairDifferent;
  private AttributePair attributePairDifferentMulti;

  @BeforeEach
  void setUp() {
    Attribute s0 = AttributeFactory.getAttribute("abcdefg");
    Attribute s1 = AttributeFactory.getAttribute("abcxefg");
    Attribute s2 = AttributeFactory.getAttribute("abcxeyg");
    attributePairSame = new AttributePair(s0, s0.duplicate());
    attributePairDifferent = new AttributePair(s0, s1);
    attributePairDifferentMulti = new AttributePair(s0, s2);
  }

  @Test
  void dice() {
    AttributeSimilarityCalculator similarityCalculator =
      new StringSimilarityCalculator(StringSimilarityCalculator.SimilarityMethod.DICE);
    double simSame = similarityCalculator.calculateSimilarity(attributePairSame);
    double simDiff = similarityCalculator.calculateSimilarity(attributePairDifferent);
    double simDiffMulti = similarityCalculator.calculateSimilarity(attributePairDifferentMulti);

    assertEquals(1.0, simSame, 0.00001);
    assertTrue(1.0 > simDiff);
    assertTrue(simDiff > simDiffMulti);
  }

  @Test
  void levenshtein() {
    AttributeSimilarityCalculator similarityCalculator =
      new StringSimilarityCalculator(StringSimilarityCalculator.SimilarityMethod.LEVENSHTEIN);
    double simSame = similarityCalculator.calculateSimilarity(attributePairSame);
    double simDiff = similarityCalculator.calculateSimilarity(attributePairDifferent);
    double simDiffMulti = similarityCalculator.calculateSimilarity(attributePairDifferentMulti);

    assertEquals(1.0, simSame, 0.00001);
    assertTrue(1.0 > simDiff);
    assertTrue(simDiff > simDiffMulti);
  }
}
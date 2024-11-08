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
import de.unileipzig.dbs.pprl.core.common.model.impl.BitSetVector;
import de.unileipzig.dbs.pprl.core.matcher.MatcherTestBase;
import de.unileipzig.dbs.pprl.core.matcher.model.AttributePair;
import de.unileipzig.dbs.pprl.core.matcher.similarityCalculation.missing.MissingSimilarityStrategy;
import org.junit.jupiter.api.Test;

import java.util.BitSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BitVectorSimilarityCalculatorTest extends MatcherTestBase {

  @Test
  void calculateSimilarity() {
    Attribute bitVector1 = AttributeFactory.getAttribute(getRandomBitVector(32));
    Attribute bitVector2 = AttributeFactory.getAttribute(getRandomBitVector(32));
    AttributePair attributePairSame = new AttributePair(bitVector1, bitVector1.duplicate());
    AttributePair attributePairDifferent = new AttributePair(bitVector1, bitVector2);

    AttributeSimilarityCalculator similarityCalculator =
      new BitVectorSimilarityCalculator(BitVectorSimilarityCalculator.SimilarityMethod.DICE);

    assertEquals(1.0, similarityCalculator.calculateSimilarity(attributePairSame), 0.00001);
    assertTrue(1.0 > similarityCalculator.calculateSimilarity(attributePairDifferent));
  }

  @Test
  void emptyBitSet() {
    Attribute bitVector1 = AttributeFactory.getAttribute(new BitSetVector(32, new BitSet()));
    AttributePair attributePair = new AttributePair(bitVector1, bitVector1.duplicate());

    AttributeSimilarityCalculator similarityCalculator =
      new BitVectorSimilarityCalculator(BitVectorSimilarityCalculator.SimilarityMethod.DICE);
    assertEquals(MissingSimilarityStrategy.MISSING_SIMILARITY,
      similarityCalculator.calculateSimilarity(attributePair),
      0.00001
    );
  }

}
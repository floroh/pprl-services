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

import de.unileipzig.dbs.pprl.core.common.BitSetUtils;
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.BitVector;
import de.unileipzig.dbs.pprl.core.matcher.model.AttributePair;
import de.unileipzig.dbs.pprl.core.matcher.similarityCalculation.missing.MissingSimilarityStrategy;

import java.util.BitSet;

public final class BitVectorSimilarityCalculator implements AttributeSimilarityCalculator {
  public enum SimilarityMethod {DICE, JACCARD, DICE_BOTH}

  private SimilarityMethod similarityMethod;

  public boolean useWeightCurve = false;

  public BitVectorSimilarityCalculator(SimilarityMethod similarityMethod) {
    this.similarityMethod = similarityMethod;
  }

  private BitVectorSimilarityCalculator() {
  }

  @Override
  public double calculateSimilarity(AttributePair attributePair) {
    final Attribute leftAttribute = attributePair.getLeftAttribute();
    final Attribute rightAttribute = attributePair.getRightAttribute();

    final BitVector leftBitVector = getAttributeValue(leftAttribute, BitVector.class);
    final BitVector rightBitVector = getAttributeValue(rightAttribute, BitVector.class);
    final int len = leftBitVector.getLength();

    double sim = getSimilarity(leftBitVector.getBitSet(), rightBitVector.getBitSet(), len);

    if (similarityMethod.toString().contains("BOTH")) {
      double isim = getSimilarity(
        BitSetUtils.invert(leftBitVector.getBitSet(), leftBitVector.getLength()),
        BitSetUtils.invert(rightBitVector.getBitSet(), rightBitVector.getLength()), len
      );
      sim = (sim + isim) / 2;
    }
    return sim;
  }

  private double getSimilarity(BitSet leftBitSet, BitSet rightBitSet, int len) {
    final int card1 = leftBitSet.cardinality();
    final int card2 = rightBitSet.cardinality();
    final int and = BitSetUtils.and(leftBitSet, rightBitSet).cardinality();

    if (card1 == 0 || card2 == 0) {
      return MissingSimilarityStrategy.MISSING_SIMILARITY;
    }

    double nom = 0;
    double divisor = 1;
    switch (similarityMethod) {
      case DICE_BOTH:
      case DICE:
        nom = 2 * and;
        divisor = card1 + card2;
        break;
      case JACCARD:
        nom = and;
        divisor = card1 + card2 - and;
        break;
    }

    double sim = divisor == 0 ? MissingSimilarityStrategy.MISSING_SIMILARITY : nom / divisor;
    sim = useWeightCurve ? applyWeightCurve(sim, card1, card2, len) : sim;
    return sim;
  }

  private double applyWeightCurve(double sim, int cardA, int cardB, int len) {
    double cutOff = getMeanRandomSimilarity(cardA, cardB, len);
    double w = weightCurve(sim, cutOff, 0.2);
    double res = sim * w;
    return res;
  }

  /**
   * Calculate weight factor
   *         0                      for x < cutOff
   * f(x) =  1                      for x > cutOff + offset
   *         (sim - cutoff)/offset  else
   * @param sim input / x
   * @param cutOff lower
   * @param offset distance between lower and upper
   * @return weight factor / f(x)
   */
  public static double weightCurve(double sim, double cutOff, double offset) {
    double x = sim - cutOff;
    if (x < 0) {
      return 0;
    }
    if (x > offset) {
      return 1;
    }
    return x / offset;
  }

  private double getMeanRandomSimilarity(int cardA, int cardB, int len) {
    double pA = (double) cardA / len;
    double pB = (double) cardB / len;

    double res = 0;
    double divisor;
    switch (similarityMethod) {
      case DICE:
      case DICE_BOTH:
        divisor = ((cardA + cardB) * len);
        res = divisor == 0 ? 0 : (2.0 * cardA * cardB) / divisor;
        break;
      case JACCARD:
        divisor = (pA + pB - pA * pB);
        res = divisor == 0 ? 0 : (pA * pB) / divisor;
        break;
    }
    return res;
  }

  public SimilarityMethod getSimilarityMethod() {
    return similarityMethod;
  }

  public boolean isUseWeightCurve() {
    return useWeightCurve;
  }

  public void setUseWeightCurve(boolean useWeightCurve) {
    this.useWeightCurve = useWeightCurve;
  }
}
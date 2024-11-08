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

import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.matcher.model.AttributePair;
import de.unileipzig.dbs.pprl.core.matcher.similarityCalculation.missing.MissingSimilarityStrategy;
import info.debatty.java.stringsimilarity.JaroWinkler;
import info.debatty.java.stringsimilarity.NormalizedLevenshtein;
import info.debatty.java.stringsimilarity.SorensenDice;
import info.debatty.java.stringsimilarity.interfaces.StringSimilarity;

public final class StringSimilarityCalculator implements AttributeSimilarityCalculator {
  public enum SimilarityMethod {LEVENSHTEIN, DICE, JAROWINKLER}

  private SimilarityMethod similarityMethod;

  private SimilarityFunction similarityFunction;

  public StringSimilarityCalculator(SimilarityMethod similarityMethod) {
    this.similarityMethod = similarityMethod;
  }

  private StringSimilarityCalculator() {
  }

  @Override
  public double calculateSimilarity(AttributePair attributePair) {
    final Attribute leftAttribute = attributePair.getLeftAttribute();
    final Attribute rightAttribute = attributePair.getRightAttribute();
    return calculateSimilarity(leftAttribute, rightAttribute);
  }

  public double calculateSimilarity(Attribute leftAttribute, Attribute rightAttribute) {
    if (similarityFunction == null) {
      similarityFunction = SimilarityFunction.fromEnum(similarityMethod);
    }
    if (leftAttribute.isEmpty() || rightAttribute.isEmpty()) {
      return MissingSimilarityStrategy.MISSING_SIMILARITY;
    }

    final String leftString = getAttributeValue(leftAttribute, String.class);
    final String rightString = getAttributeValue(rightAttribute, String.class);

    return similarityFunction.compute(leftString, rightString);
  }

  public SimilarityMethod getSimilarityMethod() {
    return similarityMethod;
  }

  private interface SimilarityFunction {

    double compute(String s0, String s1);

    static SimilarityFunction fromEnum(SimilarityMethod simEnum) {
      switch (simEnum) {
        default:
        case LEVENSHTEIN:
          return new InternalLevenshtein();
        case DICE:
          return new InternalDice();
        case JAROWINKLER:
          return new InternalJaroWinkler();
      }
    }
  }

  private static class InternalLevenshtein implements SimilarityFunction {
    private StringSimilarity f = new NormalizedLevenshtein();

    @Override
    public double compute(String s0, String s1) {
      return f.similarity(s0, s1);
    }
  }

  private static class InternalDice implements SimilarityFunction {
    private StringSimilarity f = new SorensenDice(2);

    @Override
    public double compute(String s0, String s1) {
      return f.similarity(s0, s1);
    }
  }

  private static class InternalJaroWinkler implements SimilarityFunction {
    private StringSimilarity f = new JaroWinkler();

    @Override
    public double compute(String s0, String s1) {
      return f.similarity(s0, s1);
    }
  }
}
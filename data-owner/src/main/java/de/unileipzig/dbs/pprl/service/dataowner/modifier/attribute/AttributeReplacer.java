/*
 * Copyright © 2018 - 2020 Leipzig University (Database Research Group)
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

package de.unileipzig.dbs.pprl.service.dataowner.modifier.attribute;

import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class AttributeReplacer implements AttributeModifier<String> {

  public static final String TAG_POSTFIX = "_REPLACE";
  public static final String TAG_POSTFIX_SIMILAR = TAG_POSTFIX + "_SIMILAR";
  public static final String TAG_POSTFIX_RANDOM = TAG_POSTFIX + "_RANDOM";

  private List<String> values;
  private boolean similarValue;
  private int possibleSimilarReplacementRange = 10;

  private long seed;
  private Random r;

  public AttributeReplacer(List<String> values, boolean similarValue) {
    this.values = values;
    this.similarValue = similarValue;
    this.r = new Random(123);
  }

  public AttributeReplacer(List<String> values, boolean similarValue, long seed) {
    this.values = values;
    this.similarValue = similarValue;
    this.seed = seed;
  }

  private AttributeReplacer() {
  }

  @Override
  public String modify(String in) {
    if (r == null) {
      r = new Random(seed);
    }

    if (similarValue) {
      return getSimilarValue(in);
    } else {
      return getNonEqualRandomValue(in);
    }
  }

  @Override
  public String getTagPostFix() {
    return (similarValue? TAG_POSTFIX_SIMILAR : TAG_POSTFIX_RANDOM);
  }

  private String getNonEqualRandomValue(String old) {
    String r = getRandomValue();
    while (r.equals(old)) {
      r = getRandomValue();
    }
    return r;
  }

  private String getRandomValue() {
    return values.get(r.nextInt(values.size()));
  }

  private String getSimilarValue(String original) {
    LevenshteinDistance distance = LevenshteinDistance.getDefaultInstance();
    List<StringSimilarityTuple> sorted = values.stream()
      .distinct()
      .filter(s -> !s.equals(original))
      .map(s -> new StringSimilarityTuple(s, distance.apply(s, original)))
      .sorted(Comparator.comparingInt(StringSimilarityTuple::getDistance))
      .collect(Collectors.toList());
    int randomSelection = r.nextInt(Math.min(possibleSimilarReplacementRange, sorted.size()));
    return sorted.get(randomSelection).getValue();
  }

  public int getPossibleSimilarReplacementRange() {
    return possibleSimilarReplacementRange;
  }

  public void setPossibleSimilarReplacementRange(int possibleSimilarReplacementRange) {
    this.possibleSimilarReplacementRange = possibleSimilarReplacementRange;
  }

  public List<String> getValues() {
    return values;
  }

  public boolean isSimilarValue() {
    return similarValue;
  }

  public long getSeed() {
    return seed;
  }

  private class StringSimilarityTuple {
    private String value;
    private Integer distance;

    public StringSimilarityTuple(String value, Integer distance) {
      this.value = value;
      this.distance = distance;
    }

    public String getValue() {
      return value;
    }

    public Integer getDistance() {
      return distance;
    }

    @Override
    public String toString() {
      return value + ":" + distance;
    }
  }
}

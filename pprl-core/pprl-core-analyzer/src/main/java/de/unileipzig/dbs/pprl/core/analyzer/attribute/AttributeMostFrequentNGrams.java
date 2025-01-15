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

package de.unileipzig.dbs.pprl.core.analyzer.attribute;

import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Find the most frequent ngrams per attribute type and store them as a sorted list
 */
public class AttributeMostFrequentNGrams extends AttributeFrequencyAnalyzer {

  /**
   * Length of the ngrams
   */
  private int length;

  public AttributeMostFrequentNGrams(int length) {
    super();
    this.length = length;
    logger.info("Initialized: " + this);
  }

  @Override
  public String getName() {
    return AttributeMostFrequentNGrams.getName(length);
  }

  @Override
  protected Collection<String> getValues(Attribute attribute, String attributeName) {
    if (AttributeAvailability.isInvalidOrEmpty(attributeName, attribute)) {
      return Collections.emptyList();
    }
    String value = attribute.getAsString();
    if (toLowerCase) {
      value = value.toLowerCase();
    }
    return split(value);
  }

  private Collection<String> split(String value) {
    final List<String> tokens = new ArrayList<>();

    String token = "";
    char[] chars = value.toCharArray();
    for (int i = 0; i <= chars.length - this.length; i++) {
      for (int j = i; j < i + this.length; j++) {
        token = token + chars[j];
      }
      tokens.add(token);
      token = "";
    }
    return tokens;
  }

  @Override
  protected String buildDescription() {
    //TODO Fix description and computation (result contains values higher than 1)
    return """
      Share of ngrams that belong to the (x*100)% most-frequent distinct ngrams
      where "x" is the column head and the cell entries are the shares.
      The output directory contains files for each attribute type\s
      that list distinct ngrams sorted by their frequency.""";
  }

  public static String getName(int length) {
    switch (length) {
      case 1:
        return "AttributeMostFrequentUniGrams";
      case 2:
        return "AttributeMostFrequentBiGrams";
      case 3:
        return "AttributeMostFrequentTriGrams";
      default:
        return "AttributeMostFrequent" + length + "Grams";
    }
  }

  @Override
  public String toString() {
    return "AttributeMostFrequentNGrams{" + "length=" + length + ", maxNumber=" + maxNumber + ", minCount=" +
      minCount + ", cumShares=" + cumShares + ", toLowerCase=" + toLowerCase + '}';
  }
}

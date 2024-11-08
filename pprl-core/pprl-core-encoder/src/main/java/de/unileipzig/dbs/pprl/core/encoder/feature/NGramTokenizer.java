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

package de.unileipzig.dbs.pprl.core.encoder.feature;

import java.util.HashSet;
import java.util.Set;

/**
 * Splits an attribute into a list of n-gram tokens (e.g. "Peter" -> {"Pe", "et", "te", "er"})
 *
 * @author mfranke
 */
public class NGramTokenizer implements FeatureExtractor<String, String> {
  private static final String PADDING_CHARACTER = "#";

  private int nGram;
  private boolean usePadding;
  private String paddingCharacter = PADDING_CHARACTER;

  /**
   * @param nGram      the size n of the n-grams
   * @param usePadding if true, a padding character is used for the tokens.
   * @throws RuntimeException an exception is thrown if the value of ngram is smaller then one.
   */
  public NGramTokenizer(int nGram, boolean usePadding) throws RuntimeException {
    if (nGram >= 1) {
      this.nGram = nGram;
      this.usePadding = usePadding;
    } else {
      throw new RuntimeException("n has to be larger than or equal to 0");
    }
  }

  private NGramTokenizer() {
  }

  @Override
  public Set<String> extract(String attributeValue) {

    final Set<String> tokens = new HashSet<>();

    if (attributeValue.isEmpty()) {
      return tokens;
    }

    if (this.usePadding) {
      attributeValue = this.padString(attributeValue);
    }

    String token = "";
    char[] chars = attributeValue.toCharArray();

    for (int i = 0; i <= chars.length - this.nGram; i++) {
      for (int j = i; j < i + this.nGram; j++) {
        token = token + chars[j];
      }
      tokens.add(token);
      token = "";
    }

    return tokens;
  }

  private String padString(String value) {
    String result = value;

    for (int i = 1; i < this.nGram; i++) {
      result = paddingCharacter + result + paddingCharacter;
    }

    return result;
  }

  public int getnGram() {
    return nGram;
  }

  public void setnGram(int nGram) {
    this.nGram = nGram;
  }

  public boolean isUsePadding() {
    return usePadding;
  }

  public void setUsePadding(boolean usePadding) {
    this.usePadding = usePadding;
  }

  public String getPaddingCharacter() {
    return paddingCharacter;
  }

  public void setPaddingCharacter(String paddingCharacter) {
    this.paddingCharacter = paddingCharacter;
  }

  @Override
  public String toString() {
    return "NGramTokenizer{" + "nGram=" + nGram + ", usePadding=" + usePadding + ", paddingCharacter='" +
      paddingCharacter + '\'' + '}';
  }
}
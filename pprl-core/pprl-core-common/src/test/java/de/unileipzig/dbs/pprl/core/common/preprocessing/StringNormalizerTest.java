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

package de.unileipzig.dbs.pprl.core.common.preprocessing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StringNormalizerTest {
  private String testString1 = " TestString$3 .LaTéX François";

  private StringNormalizer normalizer;

  @BeforeEach
  void setUp() {
    normalizer = new StringNormalizer().setTrim(false)
      .setDeleteWhitespace(false)
      .setToLowerCase(false)
      .setStripAccents(false);
  }

  @Test
  void everythingDisabled() {
    assertEquals(testString1, normalizer.preprocess(testString1));
  }

  @Test
  void trim() {
    normalizer.setTrim(true);
    assertEquals("TestString$3 .LaTéX François", normalizer.preprocess(testString1));
  }

  @Test
  void deleteWhiteSpace() {
    normalizer.setDeleteWhitespace(true);
    assertEquals("TestString$3.LaTéXFrançois", normalizer.preprocess(testString1));
  }

  @Test
  void toLowerCase() {
    normalizer.setToLowerCase(true);
    assertEquals(" teststring$3 .latéx françois", normalizer.preprocess(testString1));
  }

  @Test
  void stripAccents() {
    normalizer.setStripAccents(true);
    assertEquals(" TestString$3 .LaTeX Francois", normalizer.preprocess(testString1));
  }

  @Test
  void everythingEnabled() {
    normalizer.setTrim(true)
      .setDeleteWhitespace(true)
      .setToLowerCase(true)
      .setStripAccents(true);

    assertEquals("teststring$3.latexfrancois", normalizer.preprocess(testString1));
  }

}
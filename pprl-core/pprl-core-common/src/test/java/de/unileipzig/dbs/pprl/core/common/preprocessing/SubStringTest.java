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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SubStringTest {
  private String str = "abcdefg";

  @Test
  void beginWith() {
    SubString normalizer = new SubString(0, 3);
    assertEquals("abc", normalizer.preprocess(str));
  }

  @Test
  void middle() {
    SubString normalizer = new SubString(2, 5);
    assertEquals("cde", normalizer.preprocess(str));
  }

  @Test
  void shortString() {
    SubString normalizer = new SubString(0, 20);
    assertEquals(str, normalizer.preprocess(str));
  }
}
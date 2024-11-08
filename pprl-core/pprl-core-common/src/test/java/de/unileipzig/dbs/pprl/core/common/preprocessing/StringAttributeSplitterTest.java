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

import de.unileipzig.dbs.pprl.core.common.CommonTestBase;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StringAttributeSplitterTest extends CommonTestBase {

  @Test
  void onSpace() {
    StringAttributeSplitter as = new StringAttributeSplitter();

    List<String> out = as.preprocess("Peter Pan");
    assertEquals(2, out.size());
    assertEquals("Peter", out.get(0));
    assertEquals("Pan", out.get(1));
  }

  @Test
  void onHyphen() {
    StringAttributeSplitter as = new StringAttributeSplitter();

    List<String> out = as.preprocess("Müller-Wohlfahrt");
    assertEquals(2, out.size());
    assertEquals("Müller", out.get(0));
    assertEquals("Wohlfahrt", out.get(1));
  }

  @Test
  void onPoint() {
    StringAttributeSplitter as = new StringAttributeSplitter();

    List<String> out = as.preprocess("01.04.2000");
    assertEquals(3, out.size());
    assertEquals("01", out.get(0));
    assertEquals("04", out.get(1));
    assertEquals("2000", out.get(2));
  }

  @Test
  void listInput() {
    StringAttributeSplitter as = new StringAttributeSplitter();

    List<String> out = as.preprocess(Arrays.asList("Johanna", "Ann-Kathrin"));
    assertEquals(3, out.size());
    assertEquals("Johanna", out.get(0));
    assertEquals("Ann", out.get(1));
    assertEquals("Kathrin", out.get(2));
  }


}
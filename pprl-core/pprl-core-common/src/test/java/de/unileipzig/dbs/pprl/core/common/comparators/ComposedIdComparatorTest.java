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

package de.unileipzig.dbs.pprl.core.common.comparators;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ComposedIdComparatorTest {

  @Test
  void sortComposedId() {
    final List<String> ids = new ArrayList<>();
    ids.add("rec-1-org");
    ids.add("rec-2-dup");
    ids.add("rec-0-dup");
    ids.add("rec-0-org");
    ids.add("rec-1-dup");
    ids.add("rec-3-B");
    ids.add("rec-3-A");

    ids.sort(new ComposedIdComparator());

    assertEquals("rec-0-org", ids.get(0));
    assertEquals("rec-0-dup", ids.get(1));
    assertEquals("rec-1-org", ids.get(2));
    assertEquals("rec-1-dup", ids.get(3));
    assertEquals("rec-2-dup", ids.get(4));
    assertEquals("rec-3-A", ids.get(5));
    assertEquals("rec-3-B", ids.get(6));
  }

  @Test
  void compareComposedId() {
    ComposedIdComparator comparator = new ComposedIdComparator();
    String leftId = "rec-1-dup";
    String rightId = "rec-1-org";
    String addId = "rec-1-orh";

    assertTrue(comparator.compare(leftId, rightId) > 0); // dup > org
    assertTrue(comparator.compare(rightId, leftId) < 0); // org < dup
    assertTrue(comparator.compare(addId, rightId) > 0); // orh > org
  }
}
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

package de.unileipzig.dbs.pprl.core.common;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HelperUtilsTest {

  @Test
  void listOfMapsToMapOfLists() {
    List<Map<String, Integer>> listOfMaps = new ArrayList<>();
    Map<String, Integer> map = new HashMap<>();
    map.put("a", 1);
    map.put("b", 2);
    map.put("c", 3);
    listOfMaps.add(map);

    map = new HashMap<>();
    map.put("a", 10);
    map.put("b", 20);
    map.put("c", 30);
    listOfMaps.add(map);

    map = new HashMap<>();
    map.put("a", 100);
    map.put("c", 300);
    listOfMaps.add(map);

    Map<String, List<Integer>> mapOfLists = HelperUtils.listOfMapsToMapOfLists(listOfMaps);
    assertEquals(3, mapOfLists.get("a").size());
    assertTrue(mapOfLists.get("a").containsAll(Arrays.asList(1, 10, 100)));
    assertEquals(2, mapOfLists.get("b").size());
    assertTrue(mapOfLists.get("b").containsAll(Arrays.asList(2, 20)));
    assertEquals(3, mapOfLists.get("c").size());
    assertTrue(mapOfLists.get("c").containsAll(Arrays.asList(3, 30, 300)));
  }

  @Test
  void mapOfListsToListOfMaps() {
    Map<String, List<Integer>> mapOfLists = new HashMap<>();
    mapOfLists.put("a", Arrays.asList(1, 10));
    mapOfLists.put("b", Arrays.asList(2, 20));
    mapOfLists.put("c", Arrays.asList(3, 30, 300));

    List<Map<String, Integer>> listOfMaps = HelperUtils.mapOfListsToListOfMaps(mapOfLists);
    assertEquals(2 * 2 * 3, listOfMaps.size());

    Map<String, List<Integer>> mapOfListFromResult = HelperUtils.listOfMapsToMapOfLists(listOfMaps);
    assertTrue(mapOfLists.get("a").containsAll(mapOfListFromResult.get("a")));
    assertTrue(mapOfLists.get("b").containsAll(mapOfListFromResult.get("b")));
    assertTrue(mapOfLists.get("c").containsAll(mapOfListFromResult.get("c")));
  }

  @Test
  void product() {
    List<List<String>> lists = new ArrayList<>();
    lists.add(Arrays.asList("A", "B", "C"));
    lists.add(Arrays.asList("1", "2"));
    lists.add(Arrays.asList("X", "Y", "Z"));

    List<List<String>> cp = HelperUtils.product(lists);
    assertEquals(3 * 2 * 3, cp.size());
  }

  @Test
  void combination() {
    List<String> elements = Arrays.asList("A", "B", "C", "D");
    List<List<String>> combinations = HelperUtils.combination(elements, 2);
    assertEquals(6, combinations.size());

    combinations = HelperUtils.combination(elements, 3);
    assertEquals(4, combinations.size());
  }
}
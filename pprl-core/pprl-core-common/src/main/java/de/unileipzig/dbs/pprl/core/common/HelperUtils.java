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

import org.paukov.combinatorics3.Generator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class HelperUtils {

  public static double roundToTwoDigits(double in) {
    // TODO Use generic implementation based on BigDecimal
    return ((int) (in * 100)) / 100.0;
  }

  public static <T> List<T> iteratorToList(Iterator<T> iterator) {
    Iterable<T> iterable = () -> iterator;
    return StreamSupport
      .stream(iterable.spliterator(), false)
      .collect(Collectors.toList());
  }

  public static <K, V extends Comparable<? super V>> LinkedHashMap<K, V> reverseSortByValue(Map<K, V> map) {
    List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
    list.sort(Map.Entry.<K, V>comparingByValue().reversed());

    LinkedHashMap<K, V> result = new LinkedHashMap<>();
    for (Map.Entry<K, V> entry : list) {
      result.put(entry.getKey(), entry.getValue());
    }
    return result;
  }

  public static <K, V> Map<K, List<V>> listOfMapsToMapOfLists(List<Map<K, V>> listOfMaps) {
    Map<K, List<V>> mapOfLists = new HashMap<>();
    for (Map<K, V> map : listOfMaps) {
      for (Map.Entry<K, V> mapEntry : map.entrySet()) {
        if (mapOfLists.containsKey(mapEntry.getKey())) {
          List<V> list = mapOfLists.get(mapEntry.getKey());
          list.add(mapEntry.getValue());
        } else {
          List<V> list = new ArrayList<>();
          list.add(mapEntry.getValue());
          mapOfLists.put(mapEntry.getKey(), list);
        }
      }
    }
    return mapOfLists;
  }

  public static <K, V> List<Map<K, V>> mapOfListsToListOfMaps(Map<K, List<V>> mapOfLists) {
    List<Map<K, V>> listOfMaps = new ArrayList<>();

    int size = mapOfLists.size();
    List<K> keys = new ArrayList<>(size);
    List<List<V>> values = new ArrayList<>(size);
    for (Map.Entry<K, List<V>> entry : mapOfLists.entrySet()) {
      keys.add(entry.getKey());
      values.add(entry.getValue());
    }
    //TODO Add shortcut, if all lists have size 1?

    // Convert the list to an array due as the cartesian product generator expects varargs
    List[] valueArray = new List[values.size()];
    for (int i = 0; i < size; i++) {
      valueArray[i] = values.get(i);
    }

    @SuppressWarnings("unchecked") List<V>[] typedValueArray = (List<V>[]) valueArray;
    Generator.cartesianProduct(typedValueArray)
      .stream()
      .forEach(l -> {
        Map<K, V> map = new HashMap<>();
        for (int i = 0; i < size; i++) {
          map.put(keys.get(i), l.get(i));
        }
        listOfMaps.add(map);
      });
    return listOfMaps;
  }

  /**
   * Util method for generating all combinations of list entries
   * Example:
   * Input: [["A","B","C"],["1","2"]]
   * Output: [["A","1"],["A","2"],["B","1"],["B","2"],["C","1"],["C","2"]]
   *
   * @param inputLists list of lists
   * @param <T>        type of the list entries
   * @return list of possible list entry combinations
   */
  public static <T> List<List<T>> product(List<List<T>> inputLists) {
    if (inputLists.size() >= 2) {
      List<List<T>> product = new ArrayList<>();
      for (T element : inputLists.getFirst()) {
        List<T> elementList = new ArrayList<>();
        elementList.add(element);
        product.add(elementList);
      }
      for (int i = 1; i < inputLists.size(); i++) {
        product = productInner(product, inputLists.get(i));
      }
      return product;
    }
    return inputLists;
  }

  /**
   * Util method for generating all combinations of list entries with another list.
   * Example:
   * Input a: [["A","1"],["B","1"],["C","1"]]
   * Input b: ["X","Y"]
   * Output: [["A","1","X"],["A","1","Y"],["B","1","X"],["B","1","Y"],["C","1","X"],["C","1","Y"]]
   *
   * @param a   list of lists
   * @param b   list that is combined with a
   * @param <T> type of the list entries
   * @return list of possible list entry combinations
   */
  public static <T> List<List<T>> productInner(List<List<T>> a, List<T> b) {
    return Optional.of(a.stream()
        .flatMap(e1 -> b.stream()
          .map(e2 -> {
            List<T> curList = new ArrayList<>(e1);
            curList.add(e2);
            return curList;
          }))
        .collect(Collectors.toList()))
      .orElse(Collections.emptyList());
  }

  public static <T> List<List<T>> combination(Collection<T> elements, int drawnElements) {
    return Generator.combination(elements)
      .simple(drawnElements)
      .stream()
      .collect(Collectors.toList());
  }
}

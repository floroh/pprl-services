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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class HashUtilsTest {

  @Test
  void hmac() {
    List<String> keys = Arrays.asList("1a", "2b", "1a");
    List<Integer> macs = keys.stream()
      .map(k -> HashUtils.getHMAC("input", k))
      .collect(Collectors.toList());
    assertNotEquals(macs.get(0), macs.get(1));
    assertEquals(macs.get(0), macs.get(2));
  }

  @Test
  void sha() {
    List<String> keys = Arrays.asList("1a", "2b", "1a");
    List<Integer> macs = keys.stream()
      .map(HashUtils::getSHA)
      .collect(Collectors.toList());
    assertNotEquals(macs.get(0), macs.get(1));
    assertEquals(macs.get(0), macs.get(2));
  }

  @Test
  void SHA_distribution() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    LocalDate startDate = LocalDate.of(1900, 1, 1);
    int outRange = 10;
    int numberOfDays = 10000;
    List<String> testStrings = IntStream.range(0, numberOfDays)
      .mapToObj(startDate::plusDays)
      .map(date -> date.format(formatter))
      .collect(Collectors.toList());

    Map<String, Long> bins = testStrings.stream()
      .map(HashUtils::getSHA)
      .map(i -> Math.abs(i) % outRange)
      .map(String::valueOf)
      .collect(
        Collectors.groupingBy(Function.identity(), Collectors.counting()));

    bins.forEach((blk, count) -> {
      assertEquals((float) numberOfDays / outRange, count, 0.05 * (numberOfDays / outRange));
    });
  }
}
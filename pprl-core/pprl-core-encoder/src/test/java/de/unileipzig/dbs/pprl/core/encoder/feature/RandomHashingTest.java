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

import de.unileipzig.dbs.pprl.core.common.model.api.BitVector;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class RandomHashingTest {

  @Test
  void encode() {
    List<String> features = Arrays.asList("ab", "bc", "cd", "de");

    String salt = "sharedSecret";
    FeatureEncoder<String, BitVector> featureEncoder1 = new RandomHashing(4, salt);
    FeatureEncoder<String, BitVector> featureEncoder2 = new RandomHashing(10, salt);
    for (String feature : features) {
      BitVector bv1 = featureEncoder1.encode(32, feature);
      assertTrue(bv1.getCardinality() < featureEncoder2.encode(32, feature)
        .getCardinality());
    }
  }

  @Test
  void compareResults() {
    List<String> features = Arrays.asList("ab", "bcd", "ab");

    String salt1 = "sharedSecret";
    String salt2 = "anotherSharedSecret";
    FeatureEncoder<String, BitVector> featureEncoder1 = new RandomHashing(8, salt1);
    FeatureEncoder<String, BitVector> featureEncoder2 = new RandomHashing(8, salt2);

    List<BitVector> bv1 = features.stream()
      .map(f -> featureEncoder1.encode(32, f))
      .collect(Collectors.toList());

    List<BitVector> bv2 = features.stream()
      .map(f -> featureEncoder2.encode(32, f))
      .collect(Collectors.toList());

    assertNotEquals(bv1.get(0), bv2.get(0));
    assertNotEquals(bv1.get(1), bv2.get(1));
    assertEquals(bv1.get(0), bv1.get(2));
    assertEquals(bv2.get(0), bv2.get(2));
  }
}
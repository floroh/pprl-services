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

import static org.junit.jupiter.api.Assertions.assertTrue;

class DoubleHashingTest {

  @Test
  void encode() {
    List<String> features = Arrays.asList("ab", "bc", "cd", "de");

    FeatureEncoder<String, BitVector> featureEncoder1 = new DoubleHashing(4);
    FeatureEncoder<String, BitVector> featureEncoder2 = new DoubleHashing(10);
    for (String feature : features) {
      BitVector bv1 = featureEncoder1.encode(64, feature);
      assertTrue(bv1.getCardinality() < featureEncoder2.encode(64, feature)
        .getCardinality());
    }
  }

}
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

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UniPosGramsTest {

  @Test
  void extract() {
    String strAttribute = "abc";
    Collection<String> expectedQgrams = Arrays.asList("a0", "a1", "a2", "b1", "b2", "b3", "c2", "c3", "c4");
    FeatureExtractor<String, String> featureExtractor = new UniPosGrams();
    Collection<String> features = featureExtractor.extract(strAttribute);
    assertEquals(9, features.size());
    features.forEach(feature -> assertTrue(expectedQgrams.contains(feature)));
  }
}
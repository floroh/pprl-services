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

package de.unileipzig.dbs.pprl.core.encoder.attribute;

import de.unileipzig.dbs.pprl.core.common.factories.AttributeFactory;
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.BitVector;
import de.unileipzig.dbs.pprl.core.encoder.feature.DateTokenizer;
import de.unileipzig.dbs.pprl.core.encoder.feature.DoubleHashing;
import de.unileipzig.dbs.pprl.core.encoder.feature.FeatureEncoder;
import de.unileipzig.dbs.pprl.core.encoder.feature.FeatureExtractor;
import de.unileipzig.dbs.pprl.core.encoder.feature.NGramTokenizer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BitVectorEncoderTest {

  @Test
  void encode() {
    FeatureExtractor<String, String> qGramTokenizer = new NGramTokenizer(2, false);
    FeatureExtractor<String, Integer> dateTokenizer = new DateTokenizer();
    FeatureEncoder<String, BitVector> featureEncoder = new DoubleHashing(4);
    AttributeEncoder<String, BitVector> attributeEncoder = new BitVectorEncoder<>("id1", qGramTokenizer,
      featureEncoder, 128
    );
    Attribute shortAttr = AttributeFactory.getAttribute("abc");
    Attribute longAttr = AttributeFactory.getAttribute("abcdefg");
    BitVector bv1 = attributeEncoder.encode(shortAttr);
    BitVector bv2 = attributeEncoder.encode(longAttr);
    assertTrue(bv2.getCardinality() > bv1.getCardinality());
    assertEquals(128, bv1.getLength());
    assertEquals(128, bv2.getLength());
  }
}
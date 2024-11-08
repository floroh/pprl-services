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

import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.BitVector;
import de.unileipzig.dbs.pprl.core.common.model.api.ListAttribute;
import de.unileipzig.dbs.pprl.core.common.model.impl.PersonalAttributeType;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.encoder.TestBase;
import de.unileipzig.dbs.pprl.core.encoder.feature.DoubleHashing;
import de.unileipzig.dbs.pprl.core.encoder.feature.FeatureEncoder;
import de.unileipzig.dbs.pprl.core.encoder.feature.FeatureExtractor;
import de.unileipzig.dbs.pprl.core.encoder.feature.NGramTokenizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MultiAttributeEncoderGroupTest extends TestBase {

  private AttributeEncoderGroup<BitVector> aeg;

  @BeforeEach
  void setUp() {
    FeatureExtractor<String, String> featureExtractor = new NGramTokenizer(2, false);
    FeatureEncoder<String, BitVector> featureEncoder1 = new DoubleHashing(2);
    FeatureEncoder<String, BitVector> featureEncoder2 = new DoubleHashing(4);

    AttributeEncoder<?, BitVector> ae1 =
      new BitVectorEncoder<>("ae1", featureExtractor, featureEncoder1, 128);
    AttributeEncoder<?, BitVector> ae2 =
      new BitVectorEncoder<>("ae2", featureExtractor, featureEncoder2, 128);

    AttributeMerger<BitVector> bitVectorMerger = new BitVectorUnion();

    aeg = new MultiAttributeEncoderGroup<>("aeg1", bitVectorMerger);
    aeg.addAttributeEncoder(PersonalAttributeType.FIRSTNAME.toString(), ae1);
    aeg.addAttributeEncoder(PersonalAttributeType.LASTNAME.toString(), ae2);
    aeg.addAttributeEncoder(PersonalAttributeType.DATEOFBIRTH.toString(), ae2);
  }

  @Test
  void encodeSimple() {
    Record record = getPersonalRecord();
    Attribute attr = aeg.encodeToSingleAttribute(record);
    assertTrue(attr.isType(BitVector.class));

    BitVector bv = attr.getAs(BitVector.class);
    assertTrue(bv.getCardinality() > 0);
  }

  @Test
  void encodeListAttribute() {
    Record record = getPersonalRecord(2);
    Attribute attr = aeg.encodeToSingleAttribute(record).getAttribute();
    assertTrue(attr.isType(BitVector.class));

    assertTrue(attr instanceof ListAttribute);
    ListAttribute listAttr = (ListAttribute) attr;

    List<BitVector> bvs = listAttr.getListAs(BitVector.class);
    assertTrue(bvs.size() > 1);
    assertTrue(bvs.getFirst()
      .getCardinality() > 0);
  }
}
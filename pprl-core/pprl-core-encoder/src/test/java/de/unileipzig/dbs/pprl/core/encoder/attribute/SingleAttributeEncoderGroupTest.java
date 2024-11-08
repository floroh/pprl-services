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
import de.unileipzig.dbs.pprl.core.common.model.api.ListAttribute;
import de.unileipzig.dbs.pprl.core.common.model.impl.PersonalAttributeType;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.encoder.TestBase;
import de.unileipzig.dbs.pprl.core.encoder.crypto.KeyExtractor;
import de.unileipzig.dbs.pprl.core.encoder.feature.DoubleHashing;
import de.unileipzig.dbs.pprl.core.encoder.feature.FeatureEncoder;
import de.unileipzig.dbs.pprl.core.encoder.feature.FeatureExtractor;
import de.unileipzig.dbs.pprl.core.encoder.feature.NGramTokenizer;
import de.unileipzig.dbs.pprl.core.encoder.hardening.Permutation;
import de.unileipzig.dbs.pprl.core.common.preprocessing.DefaultRecordPreprocessor;
import de.unileipzig.dbs.pprl.core.common.preprocessing.StringAttributeSplitter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SingleAttributeEncoderGroupTest extends TestBase {
  private AbstractAttributeEncoderGroup<BitVector> aeg;

  @BeforeEach
  void setUp() {
    FeatureExtractor<String, String> featureExtractor = new NGramTokenizer(2, false);
    FeatureEncoder<String, BitVector> featureEncoder = new DoubleHashing(2);

    AttributeEncoder<?, BitVector> ae1 = new BitVectorEncoder<>("ae1", featureExtractor, featureEncoder, 128);

    aeg = new SingleAttributeEncoderGroup<>("aeg1", PersonalAttributeType.FIRSTNAME.toString(), ae1);
  }

  @Test
  void encodeSimple() {
    Record record = getPersonalRecord(0);
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

    BitVector bv = attr.getAs(BitVector.class);
    assertTrue(bv.getCardinality() > 0);
  }

  @Test
  void encodeWithPreprocessor() {
    DefaultRecordPreprocessor preprocessor =
      new DefaultRecordPreprocessor(
        PersonalAttributeType.FIRSTNAME.toString(),
        new StringAttributeSplitter()
      );
    aeg.addRecordPreprocessor(preprocessor);

    Record record = getPersonalRecord(2);
    Attribute attr = aeg.encodeToSingleAttribute(record).getAttribute();

    assertTrue(attr.isType(BitVector.class));
    assertTrue(attr instanceof ListAttribute);
    List<BitVector> fns = ((ListAttribute) attr).getListAs(BitVector.class);
    assertEquals(2, fns.size());
  }

  @Test
  void encodeWithKeyedHardener() {
    aeg.addKeyedHardener(new Permutation());

    Record record0 = getPersonalRecord(0);
    record0.setAttribute(KeyExtractor.KEY_ATTRIBUTE_NAME, AttributeFactory.getAttribute("a2c4"));
    Attribute attr0 = aeg.encodeToSingleAttribute(record0);
    assertTrue(attr0.isType(BitVector.class));
    BitVector bv0 = attr0.getAs(BitVector.class);

    Record record1 = getPersonalRecord(0);
    record1.setAttribute(KeyExtractor.KEY_ATTRIBUTE_NAME, AttributeFactory.getAttribute("1b3d"));
    Attribute attr1 = aeg.encodeToSingleAttribute(record1);
    assertTrue(attr1.isType(BitVector.class));
    BitVector bv1 = attr1.getAs(BitVector.class);

    assertNotEquals(bv0, bv1);
  }
}
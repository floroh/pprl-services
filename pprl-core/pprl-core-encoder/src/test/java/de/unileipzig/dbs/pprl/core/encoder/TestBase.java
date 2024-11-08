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

package de.unileipzig.dbs.pprl.core.encoder;

import de.unileipzig.dbs.pprl.core.common.factories.RecordFactory;
import de.unileipzig.dbs.pprl.core.common.factories.RecordIdFactory;
import de.unileipzig.dbs.pprl.core.common.model.impl.AttributeLight;
import de.unileipzig.dbs.pprl.core.common.model.api.BitVector;
import de.unileipzig.dbs.pprl.core.common.model.impl.ListAttributeLight;
import de.unileipzig.dbs.pprl.core.common.model.impl.PersonalAttributeType;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.encoder.attribute.AttributeEncoder;
import de.unileipzig.dbs.pprl.core.encoder.attribute.AttributeEncoderGroup;
import de.unileipzig.dbs.pprl.core.encoder.attribute.AttributeMerger;
import de.unileipzig.dbs.pprl.core.encoder.attribute.BitVectorEncoder;
import de.unileipzig.dbs.pprl.core.encoder.attribute.BitVectorUnion;
import de.unileipzig.dbs.pprl.core.encoder.attribute.HashEncoder;
import de.unileipzig.dbs.pprl.core.encoder.attribute.HashUnion;
import de.unileipzig.dbs.pprl.core.encoder.attribute.MultiAttributeEncoderGroup;
import de.unileipzig.dbs.pprl.core.encoder.attribute.SingleAttributeEncoderGroup;
import de.unileipzig.dbs.pprl.core.encoder.blocking.BlockingKeyExtractor;
import de.unileipzig.dbs.pprl.core.encoder.blocking.HashedDateOfBirth;
import de.unileipzig.dbs.pprl.core.encoder.feature.DoubleHashing;
import de.unileipzig.dbs.pprl.core.encoder.feature.FeatureEncoder;
import de.unileipzig.dbs.pprl.core.encoder.feature.FeatureExtractor;
import de.unileipzig.dbs.pprl.core.encoder.feature.NGramTokenizer;
import de.unileipzig.dbs.pprl.core.encoder.feature.RandomHashing;
import de.unileipzig.dbs.pprl.core.encoder.hardening.Balancing;
import de.unileipzig.dbs.pprl.core.encoder.hardening.Permutation;
import de.unileipzig.dbs.pprl.core.encoder.hardening.XorFolding;
import de.unileipzig.dbs.pprl.core.common.preprocessing.StringNormalizer;
import de.unileipzig.dbs.pprl.core.encoder.record.DefaultRecordEncoder;
import de.unileipzig.dbs.pprl.core.encoder.record.RecordEncoder;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class TestBase {

  protected String getTmpPath(String fileName) throws IOException {
    return File.createTempFile(fileName, "tmp")
      .getAbsolutePath();
  }

  protected String getFullPath(String path) {
    return this.getClass().getClassLoader().getResource(path).getFile();
  }

  protected RecordEncoder getRecordEncoder() {
    // Create components of the RecordEncoder
    FeatureExtractor<String, String> biGramExtractor = new NGramTokenizer(2, false);

    FeatureEncoder<String, BitVector> doubleHashingLowWeight = new DoubleHashing(2);
    FeatureEncoder<String, BitVector> doubleHashingHighWeight = new DoubleHashing(6);

    AttributeEncoder<?, BitVector> ae1 =
      new BitVectorEncoder<>("ae1", biGramExtractor, doubleHashingLowWeight, 128);
    AttributeEncoder<?, BitVector> ae2 =
      new BitVectorEncoder<>("ae2", biGramExtractor, doubleHashingHighWeight, 128);

    AttributeMerger<BitVector> bitVectorMerger = new BitVectorUnion();

    AttributeEncoderGroup<BitVector> aeg1 =
      new MultiAttributeEncoderGroup<>("aeg1", bitVectorMerger).addAttributeEncoder(
        PersonalAttributeType.FIRSTNAME.toString(), ae1)
        .addAttributeEncoder(PersonalAttributeType.LASTNAME.toString(), ae2);

    AttributeEncoderGroup<BitVector> aeg2 =
      new MultiAttributeEncoderGroup<>("aeg2", bitVectorMerger).addAttributeEncoder(
        PersonalAttributeType.FIRSTNAME.toString(), ae1)
        .addAttributeEncoder(PersonalAttributeType.LASTNAME.toString(), ae2)
        .addAttributeEncoder(PersonalAttributeType.DATEOFBIRTH.toString(), ae2);

    // Create RecordEncoder
    RecordEncoder recordEncoder = new DefaultRecordEncoder().addAttributeEncoderGroup(aeg1)
      .addAttributeEncoderGroup(aeg2);
    return recordEncoder;
  }

  protected RecordEncoder getHashEncoder() {
    // Create components of the RecordEncoder
    AttributeEncoder<String, String> ae1 = new HashEncoder("ae1", "secretA");
    AttributeEncoder<String, String> ae2 = new HashEncoder("ae2", "secretB");

    AttributeMerger<String> hashUnion = new HashUnion();

    AttributeEncoderGroup<String> aeg1 =
      new MultiAttributeEncoderGroup<String>("names", hashUnion).addAttributeEncoder(
        PersonalAttributeType.FIRSTNAME.toString(), ae1)
        .addAttributeEncoder(PersonalAttributeType.LASTNAME.toString(), ae2);

    AttributeEncoderGroup<String> aeg2 =
      new SingleAttributeEncoderGroup<>("dob", PersonalAttributeType.DATEOFBIRTH.toString(), ae1);

    // Create RecordEncoder
    RecordEncoder recordEncoder = new DefaultRecordEncoder().addAttributeEncoderGroup(aeg1)
      .addAttributeEncoderGroup(aeg2);
    return recordEncoder;
  }

  protected RecordEncoder getRecordEncoderWithBlocking() {
    RecordEncoder re = getRecordEncoder();

    BlockingKeyExtractor bke =
      new HashedDateOfBirth("hdob", PersonalAttributeType.DATEOFBIRTH.toString(), "yyyy-mm-dd", true);
    re.addBlockingKeyExtractor(bke);
    return re;
  }

  protected RecordEncoder getFullFeaturedRecordEncoder() {
    FeatureExtractor<String, String> biGramExtractor = new NGramTokenizer(2, false);
    FeatureEncoder<String, BitVector> dh = new DoubleHashing(6);

    AttributeEncoder<?, BitVector> ae1 =
      new BitVectorEncoder<>("ae1", biGramExtractor, dh, 128).setPreprocessor(new StringNormalizer());

    FeatureEncoder<String, BitVector> rh = new RandomHashing(20, "secretSalt");
    AttributeEncoder<?, BitVector> ae2 = new BitVectorEncoder<>("ae1", biGramExtractor, rh, 128);
    AttributeMerger<BitVector> bitVectorMerger = new BitVectorUnion();

    AttributeEncoderGroup<BitVector> aeg1 =
      new MultiAttributeEncoderGroup<>("aeg1", bitVectorMerger).addAttributeEncoder(
        PersonalAttributeType.FIRSTNAME.toString(), ae1)
        .addAttributeEncoder(PersonalAttributeType.LASTNAME.toString(), ae2)
        .addHardener(new XorFolding())
        .addKeyedHardener(new Permutation());

    AttributeEncoderGroup<BitVector> aeg2 =
      new MultiAttributeEncoderGroup<>("aeg2", bitVectorMerger).addAttributeEncoder(
        PersonalAttributeType.FIRSTNAME.toString(), ae1)
        .addAttributeEncoder(PersonalAttributeType.LASTNAME.toString(), ae1)
        .addAttributeEncoder(PersonalAttributeType.DATEOFBIRTH.toString(), ae2)
        .addHardener(new Balancing(1234L));

    // Create RecordEncoder
    RecordEncoder recordEncoder = new DefaultRecordEncoder();
    recordEncoder.addAttributeEncoderGroup(aeg1);
    recordEncoder.addAttributeEncoderGroup(aeg2);
    return recordEncoder;
  }

  protected Record getPersonalRecord() {
    return getPersonalRecord(0);
  }

  protected Record getPersonalRecord(int i) {
    Record record = RecordFactory.getEmptyRecord(RecordIdFactory.get("record" + i));
    switch (i) {
      case 0:
        record.setAttribute(PersonalAttributeType.FIRSTNAME.toString(), new AttributeLight("Peter"))
          .setAttribute(PersonalAttributeType.LASTNAME.toString(), new AttributeLight("Pan"))
          .setAttribute(PersonalAttributeType.DATEOFBIRTH.toString(), new AttributeLight("29.01.2019"));
        break;
      case 1:
        record.setAttribute(PersonalAttributeType.FIRSTNAME.toString(), new AttributeLight("Petra"))
          .setAttribute(PersonalAttributeType.LASTNAME.toString(), new AttributeLight("Pan"))
          .setAttribute(PersonalAttributeType.DATEOFBIRTH.toString(), new AttributeLight("29.10.2019"));
        break;
      case 2:
        record.setAttribute(
          PersonalAttributeType.FIRSTNAME.toString(),
          new ListAttributeLight(Arrays.asList("Petra", "Johanna"))
        )
          .setAttribute(
            PersonalAttributeType.LASTNAME.toString(),
            new ListAttributeLight(Arrays.asList("Pan", "Hook"))
          )
          .setAttribute(PersonalAttributeType.DATEOFBIRTH.toString(), new AttributeLight("29.10.2019"));
        break;
      case 3:
        record.setAttribute(PersonalAttributeType.FIRSTNAME.toString(), new AttributeLight("Petra Johanna"))
          .setAttribute(PersonalAttributeType.LASTNAME.toString(), new AttributeLight("Pan-Hook"))
          .setAttribute(PersonalAttributeType.DATEOFBIRTH.toString(), new AttributeLight("29.10.2019"));
        break;
    }
    return record;
  }


}

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

package de.unileipzig.dbs.pprl.core.encoder.record;

import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.BitVector;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.encoder.TestBase;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultRecordEncoderTest extends TestBase {

  @Test
  void encodeSmall() {
    RecordEncoder recordEncoder = getRecordEncoder();

    // Create record instance
    Record record = getPersonalRecord();

    // Encode the test record
    Record encodedRecord = recordEncoder.encode(record);

    // Check that id of the record is the same for the encoded record
    assertEquals(record.getId(), encodedRecord.getId());

    // Check that the encoded record has two bitvectors with the correct ids
    assertEquals(2, encodedRecord.getAttributes()
      .size());
    Optional<Attribute> attr1 = encodedRecord.getAttribute("aeg1");
    Optional<Attribute> attr2 = encodedRecord.getAttribute("aeg2");
    assertTrue(attr1.isPresent());
    assertTrue(attr2.isPresent());
    assertTrue(attr1.get()
      .isType(BitVector.class));
    assertTrue(attr2.get()
      .isType(BitVector.class));

    // In the used configuration attr2 contains FIRSTNAME, LASTNAME and DATEOFBIRTH
    // whereas attr1 only contains FIRSTNAME and LASTNAME and therefore less bits are set
    BitVector bv1 = (BitVector) attr1.get()
      .getObject();
    BitVector bv2 = (BitVector) attr2.get()
      .getObject();
    assertTrue(bv2.getCardinality() > bv1.getCardinality());
  }


}
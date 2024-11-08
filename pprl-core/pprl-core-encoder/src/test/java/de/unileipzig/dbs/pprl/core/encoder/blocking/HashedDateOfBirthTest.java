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

package de.unileipzig.dbs.pprl.core.encoder.blocking;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.unileipzig.dbs.pprl.core.common.factories.AttributeFactory;
import de.unileipzig.dbs.pprl.core.common.factories.RecordFactory;
import de.unileipzig.dbs.pprl.core.common.model.api.BlockingKey;
import de.unileipzig.dbs.pprl.core.common.model.impl.PersonalAttributeType;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.encoder.TestBase;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class HashedDateOfBirthTest extends TestBase {

  @Test
  void extract() {
    Record record0 = RecordFactory.getEmptyRecord();
    record0.setAttribute(
      PersonalAttributeType.DATEOFBIRTH.toString(),
      AttributeFactory.getAttribute("2019-11-04")
    );

    Record record1 = RecordFactory.getEmptyRecord();
    record1.setAttribute(
      PersonalAttributeType.DATEOFBIRTH.toString(),
      AttributeFactory.getAttribute("2019-04-11")
    );

    Record record2 = RecordFactory.getEmptyRecord();
    record2.setAttribute(
      PersonalAttributeType.DATEOFBIRTH.toString(),
      AttributeFactory.getAttribute("2019-04-30")
    );

    HashedDateOfBirth hashedDateOfBirth =
      new HashedDateOfBirth("HDOB", PersonalAttributeType.DATEOFBIRTH.toString(), "yyyy-MM-dd", "yyyyMMdd",
        "19000101", "19000301", true
      );

    Set<BlockingKey> blks0 = hashedDateOfBirth.extract(record0);
    Set<BlockingKey> blks1 = hashedDateOfBirth.extract(record1);
    Set<BlockingKey> blks2 = hashedDateOfBirth.extract(record2);

    blks0.forEach(blk -> assertFalse(blk.getId()
      .isEmpty()));
    blks0.forEach(blk -> assertFalse(blk.getValue()
      .isEmpty()));
    assertEquals(2, blks0.size());
    assertTrue(blks1.containsAll(blks0));

    // Month and day of record2 cannot be switched
    assertEquals(1, blks2.size());
  }

  @Test
  void serialize() throws Exception {
    Record record0 = RecordFactory.getEmptyRecord();
    record0.setAttribute(
      PersonalAttributeType.DATEOFBIRTH.toString(),
      AttributeFactory.getAttribute("2019-11-04")
    );

    HashedDateOfBirth hashedDateOfBirth =
      new HashedDateOfBirth("HDOB", PersonalAttributeType.DATEOFBIRTH.toString(), "yyyy-MM-dd", "yyyy.MM.dd",
        "1901.01.01", "1901.03.31", true
      );

    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writerWithDefaultPrettyPrinter()
      .writeValueAsString(hashedDateOfBirth);
    HashedDateOfBirth clone = mapper.readValue(json, HashedDateOfBirth.class);
    assertNotNull(clone);
  }

}
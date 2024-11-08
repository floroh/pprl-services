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

import com.fasterxml.jackson.databind.ObjectMapper;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.encoder.blocking.BlockingKeyExtractor;
import de.unileipzig.dbs.pprl.core.encoder.blocking.ColognePhonetic;
import de.unileipzig.dbs.pprl.core.encoder.blocking.Equality;
import de.unileipzig.dbs.pprl.core.encoder.blocking.EqualityMulti;
import de.unileipzig.dbs.pprl.core.encoder.blocking.HLSH;
import de.unileipzig.dbs.pprl.core.encoder.blocking.Soundex;
import de.unileipzig.dbs.pprl.core.encoder.record.RecordEncoder;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecordEncoderSerializationTest extends TestBase {

  @Test
  void recordEncoder() throws Exception {
    RecordEncoder recordEncoder = getRecordEncoder();

    String jsonString = RecordEncoderSerialization.serializeJson(recordEncoder);
    assertFalse(jsonString.isEmpty());

    RecordEncoder clone = RecordEncoderSerialization.deserializeJson(jsonString);
    assertNotNull(clone);
  }

  @Test
  void hashEncoder() throws Exception {
    RecordEncoder recordEncoder = getHashEncoder();

    String jsonString = RecordEncoderSerialization.serializeJson(recordEncoder);
    assertFalse(jsonString.isEmpty());

    RecordEncoder clone = RecordEncoderSerialization.deserializeJson(jsonString);
    assertNotNull(clone);
  }

  @Test
  void fullyFeaturedRecordEncoder() throws Exception {
    RecordEncoder recordEncoder = getFullFeaturedRecordEncoder();

    String jsonString = RecordEncoderSerialization.serializeJson(recordEncoder);
    assertFalse(jsonString.isEmpty());

    RecordEncoder clone = RecordEncoderSerialization.deserializeJson(jsonString);
    assertNotNull(clone);
  }

  @Test
  void blockingKeyExtractor() throws Exception {
    List<BlockingKeyExtractor> bkes = new ArrayList<>();
    bkes.add(new Equality("eq", "FIRSTNAME"));
    bkes.add(new EqualityMulti("eqM", "FIRSTNAME", "LASTNAME"));
    bkes.add(new HLSH("hslh", "FIRSTNAME", 512, 1357L, 7, 23));
    bkes.add(new Soundex("soundex", "FIRSTNAME"));
    bkes.add(new ColognePhonetic("soundex", "FIRSTNAME"));

    for (BlockingKeyExtractor bke : bkes) {
      String jsonString = new ObjectMapper().writerWithDefaultPrettyPrinter()
        .writeValueAsString(bke);
      BlockingKeyExtractor clone = new ObjectMapper().readValue(jsonString, BlockingKeyExtractor.class);
      assertEquals(bke, clone);
    }
  }

  @Test
  void record() throws Exception {
    Record record = getPersonalRecord();
    String json = new ObjectMapper().writerWithDefaultPrettyPrinter()
      .writeValueAsString(record);
    assertNotNull(json);
  }
}
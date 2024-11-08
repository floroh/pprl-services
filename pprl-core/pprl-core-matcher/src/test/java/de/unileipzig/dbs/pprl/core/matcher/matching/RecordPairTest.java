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

package de.unileipzig.dbs.pprl.core.matcher.matching;

import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordId;
import de.unileipzig.dbs.pprl.core.matcher.MatcherTestBase;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import de.unileipzig.dbs.pprl.core.common.model.impl.RecordPairSimple;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecordPairTest extends MatcherTestBase {

  @Test
  void testEquals() throws IOException {
    Record leftRecord = getBitVectorRecord();
    leftRecord.getId().addId(RecordId.SOURCE_ID, "srcA");
    leftRecord.getId().addId(RecordId.LOCAL_ID, "1");
    Record rightRecord = getBitVectorRecord();
    rightRecord.getId().addId(RecordId.SOURCE_ID, "srcB");
    rightRecord.getId().addId(RecordId.LOCAL_ID, "2");
    assertFalse(leftRecord.equals(rightRecord));
    assertFalse(rightRecord.equals(leftRecord));
    RecordPair rp = new RecordPairSimple(leftRecord, rightRecord);
    RecordPair rpSwitched = new RecordPairSimple(rightRecord, leftRecord);
    assertTrue(rp.equals(rpSwitched));
  }
}
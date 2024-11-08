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

package de.unileipzig.dbs.pprl.core.matcher.blocking;

import de.unileipzig.dbs.pprl.core.common.factories.RecordFactory;
import de.unileipzig.dbs.pprl.core.common.factories.RecordIdFactory;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordCluster;
import de.unileipzig.dbs.pprl.core.common.model.impl.RecordClusterSimple;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordId;
import de.unileipzig.dbs.pprl.core.matcher.MatcherTestBase;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CrossProductTest extends MatcherTestBase {

  @Test
  void blockOne() {
    RecordCluster cluster = generateRecordCluster("src0", 500);
    assertEquals(500, cluster.getRecords().size());

    CrossProduct crossProduct = new CrossProduct();
    Set<RecordPair> pairs = crossProduct.blockOne(cluster.getRecords());
    assertEquals(500 * 499 / 2, pairs.size());
  }

  @Test
  void blockTwo() {
    List<RecordCluster> cluster =
      Arrays.asList(generateRecordCluster("src0", 57), generateRecordCluster("src1", 31));
    assertEquals(57, cluster.get(0).getRecords().size());
    assertEquals(31, cluster.get(1).getRecords().size());

    CrossProduct crossProduct = new CrossProduct();
    Set<RecordPair> pairs = crossProduct.blockTwo(cluster.get(0).getRecords(), cluster.get(1).getRecords());
    assertEquals(57 * 31, pairs.size());
  }

  @Test
  void blockN() {
    final int n1 = 57;
    final int n2 = 31;
    final int n3 = 12;
    List<RecordCluster> cluster = Arrays
      .asList(generateRecordCluster("A", n1), generateRecordCluster("B", n2), generateRecordCluster("C", n3));
    assertEquals(n1, cluster.get(0).getRecords().size());
    assertEquals(n2, cluster.get(1).getRecords().size());
    assertEquals(n3, cluster.get(2).getRecords().size());

    CrossProduct crossProduct = new CrossProduct();
    Set<RecordPair> pairs = crossProduct.blockN(cluster);
    assertEquals(n1 * n2 + n1 * n3 + n2 * n3, pairs.size());
  }

  private RecordCluster generateRecordCluster(String name, int size) {
    Collection<Record> records = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      RecordId recordId = RecordIdFactory.get(String.valueOf(i));
      recordId.addId(RecordId.SOURCE_ID, name);
      records.add(RecordFactory.getEmptyRecord(recordId));
    }
    return new RecordClusterSimple(records);
  }
}
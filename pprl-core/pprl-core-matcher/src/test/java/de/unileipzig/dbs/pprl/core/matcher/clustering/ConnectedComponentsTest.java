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

package de.unileipzig.dbs.pprl.core.matcher.clustering;

import de.unileipzig.dbs.pprl.core.common.factories.RecordFactory;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordCluster;
import de.unileipzig.dbs.pprl.core.common.model.impl.RecordIdComposed;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import de.unileipzig.dbs.pprl.core.common.model.impl.RecordPairSimple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConnectedComponentsTest {

  Set<RecordPair> recordPairs;

  @BeforeEach
  void setUp() {
    recordPairs = new HashSet<>();
    recordPairs.add(getPair("1.1", "1.2", 0.8, "org"));
    recordPairs.add(getPair("1.1", "1.3", 0.9, "org"));
    recordPairs.add(getPair("1.3", "1.4", 0.7, "dup"));
    recordPairs.add(getPair("2.1", "2.2", 0.6, "org"));
    recordPairs.add(getPair("2.3", "2.2", 0.5, "org"));
  }

  @Test
  void cluster() {
    ConnectedComponents cc = new ConnectedComponents();
    Collection<RecordCluster> cluster = cc.cluster(recordPairs);
    assertEquals(2, cluster.size());

    int ccCount = cluster.stream().mapToInt(c -> c.getRecords().size()).sum();
    assertEquals(7, ccCount);

    for (RecordCluster recordCluster : cluster) {
      String clusterPrefix = null;
      for (Record record : recordCluster.getRecords()) {
        String[] idParts = record.getId().getLocalId().split("\\.");
        if (clusterPrefix == null) {
          clusterPrefix = idParts[0];
        } else {
          assertEquals(clusterPrefix, idParts[0]);
        }
      }
    }
  }

  private RecordPair getPair(String left, String right, Double sim, String leftSrc) {
//		int rnd = new Random().nextInt(2);
//		String leftSrc = rnd > 0? "org" : "dup";
    String rightSrc = leftSrc.equals("org") ? "dup" : "org";

    return new RecordPairSimple(
      RecordFactory.getEmptyRecord(RecordIdComposed.of("rec-" + left + "-" + leftSrc)),
      RecordFactory.getEmptyRecord(RecordIdComposed.of("rec-" + right + "-" + rightSrc)),
      sim
    );
  }
}
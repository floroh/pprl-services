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


import de.unileipzig.dbs.pprl.core.common.model.api.BlockingKey;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordCluster;
import de.unileipzig.dbs.pprl.core.encoder.blocking.BlockingKeyExtractor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Generate blocking keys according to the used {@link BlockingKeyExtractor}s
 * and create {@link BlockingGroup}s of records, that are share a blocking key.
 */
public class StandardBlocking implements Blocker {
  private Collection<BlockingKeyExtractor> blockingKeyExtractors;

  public StandardBlocking() {
    blockingKeyExtractors = new ArrayList<>();
  }

  @Override
  public Collection<BlockingKey> generateKeys(Record record) {
    Set<BlockingKey> bks = new HashSet<>();
    for (BlockingKeyExtractor bke : blockingKeyExtractors) {
      bks.addAll(bke.extract(record));
    }
    return bks;
  }

  @Override
  public Collection<BlockingGroup> block(Collection<RecordCluster> sources) {
    final Map<BlockingKey, List<RecordCluster>> allRecordCluster = new HashMap<>();
    for (RecordCluster source : sources) {
      Map<BlockingKey, RecordCluster> curRecordCluster = new HashMap<>();
      for (Record record : source.getRecords()) {
        Collection<BlockingKey> bks = generateKeys(record);
        for (BlockingKey bk : bks) {
          if (!curRecordCluster.containsKey(bk)) {
            curRecordCluster.put(bk, record.getCluster());
          }
          curRecordCluster.get(bk).addRecord(record);
        }
      }
      for (Map.Entry<BlockingKey, RecordCluster> group : curRecordCluster.entrySet()) {
        if (!allRecordCluster.containsKey(group.getKey())) {
          allRecordCluster.put(group.getKey(), new ArrayList<>());
        }
        allRecordCluster.get(group.getKey()).add(group.getValue());
      }
    }
    final Collection<BlockingGroup> blockingGroups = new HashSet<>();
    for (Map.Entry<BlockingKey, List<RecordCluster>> blockingKeyListEntry : allRecordCluster.entrySet()) {
      blockingGroups.add(new BlockingGroupInMemory(blockingKeyListEntry.getKey(),
        blockingKeyListEntry.getValue()));
    }
    return blockingGroups;
  }

  public Collection<BlockingKeyExtractor> getBlockingKeyExtractors() {
    return blockingKeyExtractors;
  }

  public void setBlockingKeyExtractors(Collection<BlockingKeyExtractor> blockingKeyExtractors) {
    this.blockingKeyExtractors = blockingKeyExtractors;
  }

  public Blocker addBlockingKeyExtractor(BlockingKeyExtractor blockingKeyExtractor) {
    blockingKeyExtractors.add(blockingKeyExtractor);
    return this;
  }
}

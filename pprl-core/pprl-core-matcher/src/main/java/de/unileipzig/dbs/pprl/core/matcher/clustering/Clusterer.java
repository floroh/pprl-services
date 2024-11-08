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

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordCluster;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordId;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Group records that belong to the same real world entity
 * based on the computed similarity links between the records
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property = "@class")
public interface Clusterer {

  <P extends RecordPair> Set<RecordCluster> cluster(Collection<P> recordPairs);

  default <P extends RecordPair> Collection<Record> assignGlobalId(Collection<P> recordPairs) {
    int i = 0;
    List<Record> records = new ArrayList<>();
    for (RecordCluster cluster : cluster(recordPairs)) {
      for (Record record : cluster.getRecords()) {
        record.getId().addId(RecordId.GLOBAL_ID, String.valueOf(i));
        records.add(record);
      }
      i++;
    }
    return records;
  }
}

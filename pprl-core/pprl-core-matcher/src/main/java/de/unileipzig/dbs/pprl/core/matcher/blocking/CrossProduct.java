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

import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordCluster;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import org.apache.commons.collections4.set.ListOrderedSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Generate pairs of {@link Record} from multiple record cluster, which are considered "clean",
 * using the cross product which means that each record of source A will be compared to each of source B.
 * The quadratic complexity of this simple approach leads to a huge
 * amount of comparisons which makes it applicable for small datasets only.
 *
 * @see Blocker
 */
public class CrossProduct {

  private Predicate<RecordPair> filter = (recordPair -> true);

  private final static Logger logger = LogManager.getLogger(CrossProduct.class);
  
  //TODO Benchmark blockTwo vs blockN(2)
  public Set<RecordPair> block(Collection<RecordCluster> sources) {
//		if (sources.size() == 1) {
//			return blockOne(new ArrayList<>(sources).get(0).getRecords());
//		} else if (sources.size() == 2) {
    if (sources.size() == 2) {
      List<RecordCluster> sourcesAsList = new ArrayList<>(sources);
      return blockTwo(sourcesAsList.get(0).getRecords(), sourcesAsList.get(1).getRecords());
    }
    return blockN(sources);
  }

  public Set<RecordPair> blockOne(Collection<Record> recordSet) {
    final Set<RecordPair> recordPairs = new HashSet<>(recordSet.size() * (recordSet.size() - 1) / 2);
    final List<Record> recordList = new ArrayList<>(recordSet);
    for (int i = 0; i < recordList.size(); i++) {
      for (int j = i + 1; j < recordList.size(); j++) {
        getRecordPair(recordList.get(i), recordList.get(j)).ifPresent(recordPairs::add);
      }
    }
    return recordPairs;
  }

  public Set<RecordPair> blockTwo(Collection<Record> recordSetA, Collection<Record> recordSetB) {
    final Set<RecordPair> recordPairs = new HashSet<>();
    final int sizeA = recordSetA.size();
    final int sizeB = recordSetB.size();
    final Collection<Record> smallerDataset = sizeA <= sizeB ? recordSetA : recordSetB;
    final Collection<Record> biggerDataset = sizeA > sizeB ? recordSetA : recordSetB;
    long curPair = 0;
    long pairs = (long) sizeA * sizeB;
    if (pairs > 10000000) {
      logger.warn("Skipping blocking of current group, too many pairs: " + pairs);
      return recordPairs;
    }
    for (final Record recA : smallerDataset) {
      for (final Record recB : biggerDataset) {
        curPair++;
        if (pairs > 1000000 && curPair % 1000000 == 0) {
          logger.debug("Processed pairs: " + curPair + "/" + pairs);
        }
        getRecordPair(recA, recB).ifPresent(recordPairs::add);
      }
    }
    return recordPairs;
  }

  public Set<RecordPair> blockN(Collection<RecordCluster> sources) {
    final Set<RecordPair> recordPairs = new ListOrderedSet<>();
    if (sources.size() <= 1) {
      return recordPairs;
    }

    final Collection<Record> stack = new ArrayList<>();
    int i = 0;
    for (RecordCluster source : sources) {
      if (i > 0) {
        for (final Record recA : stack) {
          for (final Record recB : source.getRecords()) {
            getRecordPair(recA, recB).ifPresent(recordPairs::add);
          }
        }
      }
      i++;
      stack.addAll(source.getRecords());
    }
    return recordPairs;
  }

  public void setFilter(Predicate<RecordPair> filter) {
    this.filter = filter;
  }

  private Optional<RecordPair> getRecordPair(Record recA, Record recB) {
    final RecordPair p = recA.getPair(recB);
    return Optional.ofNullable(filter.test(p) ? p : null);
  }
}

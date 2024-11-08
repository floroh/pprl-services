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

package de.unileipzig.dbs.pprl.core.matcher.matcher;

import de.unileipzig.dbs.pprl.core.common.model.api.BlockedDataSet;
import de.unileipzig.dbs.pprl.core.common.model.api.BlockingKey;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordId;
import de.unileipzig.dbs.pprl.core.matcher.blocking.Blocker;
import de.unileipzig.dbs.pprl.core.matcher.linking.Linker;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import de.unileipzig.dbs.pprl.core.matcher.model.api.SearchResult;
import de.unileipzig.dbs.pprl.core.matcher.model.api.SearchResultEntry;
import de.unileipzig.dbs.pprl.core.matcher.model.impl.BasicSearchResult;
import de.unileipzig.dbs.pprl.core.matcher.model.impl.BasicSearchResultEntry;
import de.unileipzig.dbs.pprl.core.common.model.impl.RecordPairSimple;
import io.micrometer.core.instrument.Metrics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Simple incremental matcher that keeps one record per global id.
 * Inserted records are compared to that record only.
 */
public class SingletonIncrementalMatcher implements IncrementalMatcher {

  private Blocker blocker;

  private Linker linker;

  private BlockedDataSet dataSet;

  private final static Logger logger = LogManager.getLogger(SingletonIncrementalMatcher.class);

  public SingletonIncrementalMatcher(Blocker blocker, Linker linker) {
    this.blocker = blocker;
    this.linker = linker;
  }

  private SingletonIncrementalMatcher() {
  }

  @Override
  public void setDataSet(BlockedDataSet dataSet) {
    this.dataSet = dataSet;
  }

  @Override
  public SearchResult search(Record query) {
    return search(query, blocker.generateKeys(query));
  }

  @Override
  public RecordId insert(Record query) {
    Collection<BlockingKey> blockingKeys = Metrics.timer("generate.blockingkeys.time")
      .record(() -> blocker.generateKeys(query));

    SearchResult searchResult = Metrics.timer("search.time").record(()->search(query, blockingKeys));

    RecordId recordId = Metrics.timer("assign.time").record(()->assignRecordId(query, blockingKeys,
      searchResult));
    return recordId;
  }

  private RecordId assignRecordId(Record query, Collection<BlockingKey> blockingKeys,
    SearchResult searchResult) {
    RecordId recordId = query.getId();
    Optional<Record> bestMatch = searchResult.bestMatch();
    if (bestMatch.isPresent()) {
      assignOtherGlobalId(recordId, bestMatch.get());
    } else {
      assignNewGlobalId(recordId);
      query.setId(recordId);
      Metrics.timer("persist.newrecord.time").record(() -> dataSet.addBlockedRecord(query, blockingKeys));
    }
    return recordId;
  }

  public SearchResult search(Record query, Collection<BlockingKey> blockingKeys) {
    Collection<Record> candidates =
      Metrics.timer("candidates.fetch.time").record(()->dataSet.getRecordsByBlockingKeys(blockingKeys));
    logger.info("Found " + candidates.size() + " candidates for record " + query.getId().toString());
    Metrics.summary("candidates.count").record(candidates.size());
    List<RecordPair> result =
      Metrics.timer("candidates.comparison.time").record(() -> compare(query, candidates));
    BasicSearchResult searchResult = new BasicSearchResult();
    for (RecordPair pair : result) {
      SearchResultEntry entry = new BasicSearchResultEntry(pair.getRightRecord(), pair.getSimilarity());
      searchResult.addEntry(entry);
    }
    return searchResult;
  }

  private List<RecordPair> compare(Record newRecord, Collection<Record> records) {
    List<RecordPair> recordPairs = records.stream()
      .map(r -> new RecordPairSimple(newRecord, r))
      .collect(Collectors.toList());

    return linker.compareAndClassify(recordPairs).stream()
      .sorted(Comparator.comparingDouble(RecordPair::getSimilarity).reversed())
      .collect(Collectors.toList());
  }

  private void assignOtherGlobalId(RecordId recordId, Record other) {
    String bestMatchGlobalid = other.getId().getId(
      RecordId.GLOBAL_ID);
    logger.info("Found best match: " + bestMatchGlobalid);
    recordId.addId(RecordId.GLOBAL_ID, bestMatchGlobalid);
  }

  private void assignNewGlobalId(RecordId recordId) {
    //TODO Use generic IDGenerator
    String newGlobalId = UUID.randomUUID().toString();
    logger.info("Assigned new global id: " + newGlobalId);
    recordId.addId(RecordId.GLOBAL_ID, newGlobalId);
  }

  public Blocker getBlocker() {
    return blocker;
  }

  public void setBlocker(Blocker blocker) {
    this.blocker = blocker;
  }

  public Linker getLinker() {
    return linker;
  }

  public void setLinker(Linker linker) {
    this.linker = linker;
  }
}


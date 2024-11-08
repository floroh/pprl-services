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

import de.unileipzig.dbs.pprl.core.common.RecordUtils;
import de.unileipzig.dbs.pprl.core.common.model.api.BlockingKey;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordCluster;
import de.unileipzig.dbs.pprl.core.common.monitoring.Tag;
import de.unileipzig.dbs.pprl.core.matcher.blocking.Blocker;
import de.unileipzig.dbs.pprl.core.matcher.blocking.BlockingGroup;
import de.unileipzig.dbs.pprl.core.matcher.blocking.CrossProduct;
import de.unileipzig.dbs.pprl.core.matcher.classification.Classifier;
import de.unileipzig.dbs.pprl.core.matcher.clustering.Clusterer;
import de.unileipzig.dbs.pprl.core.matcher.linking.Linker;
import de.unileipzig.dbs.pprl.core.matcher.model.api.LinkageProcessDataSet;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import de.unileipzig.dbs.pprl.core.matcher.postprocessing.LinksPostprocessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static de.unileipzig.dbs.pprl.core.matcher.model.api.LinkageProcessDataSet.*;

/**
 * Batch matcher that uses a dataset to store intermediate results such as links between records
 * Benefits:
 * - linkage process can be run phase by phase without having to manage the state externally
 * - linkage of large datasets where we would get an OutOfMemory exception because all records are kept
 * in memory instead of being linked block by block
 */
public class DatasetBasedBatchMatcher implements BatchMatcher {

  public static final String CHANGED_BY_RECLASSIFICATION = "CHANGED_BY_RECLASSIFICATION";
  private Blocker blocker;
  private Linker linker;
  private LinksPostprocessor linksPostprocessor;
  private Clusterer clusterer;

  private LinkageProcessDataSet dataSet;

  private final static Logger logger = LogManager.getLogger(DatasetBasedBatchMatcher.class);

  public DatasetBasedBatchMatcher(Blocker blocker, Linker linker,
    Clusterer clusterer) {
    this.blocker = blocker;
    this.linker = linker;
    this.clusterer = clusterer;
  }

  private DatasetBasedBatchMatcher() {
  }

  public void setDataSet(LinkageProcessDataSet dataSet) {
    this.dataSet = dataSet;
  }

  public void runAll() {
    runBlockedLinking();
    runPostProcessing();
    runClustering();
  }

  public void runBlocking() {
    logger.info("Running blocking");
    final Collection<Record> records = dataSet.getAllRecords();
    logger.info("Blocking {} records", records.size());

    // Compute blockingKeys
    Set<BlockingKey> blockingKeys = records.parallelStream()
      .flatMap(r -> blocker.generateKeys(r).stream())
      .collect(Collectors.toSet());
    throw new RuntimeException("Not implemented yet");
    // TODO Persist blockingKeys
  }

  public void runBlockedLinking() {
    final Collection<Record> records = dataSet.getAllRecords();
    if (records.isEmpty()) {
      logger.warn("Running blocked linking on an empty dataset");
    }
    runBlockedLinking(records);
  }

  public void runBlockedLinking(Collection<Record> records) {
    logger.info("Running blocked linking on {} records", records.size());

    logger.info("Blocking {} records", records.size());
    final List<RecordCluster> sourceGroups = RecordUtils.getSourceGroups(records);
    final Collection<BlockingGroup> blockingGroups = blocker.block(sourceGroups);
    logger.info("Number of blocking groups: " + blockingGroups.size());
    dataSet.addBlockingGroups(blockingGroups);

    final CrossProduct crossProduct = new CrossProduct();
    crossProduct.setFilter(pair -> linker.compareAndClassify(pair).isPresent());

    final Set<String> recordIdPairs = new HashSet<>();
    long c = 0;
    long all = blockingGroups.size();
    for (BlockingGroup blockingGroup : blockingGroups) {
      if (blockingGroup.getGroups().size() == 2) {
        List<RecordCluster> sourcesAsList = new ArrayList<>(blockingGroup.getGroups());
        if ((sourcesAsList.get(0).getRecords().size() * sourcesAsList.get(1).getRecords().size()) > 1000000) {
          logger.warn(
            "Skipping blocking group with more than 1 million pairs: " + blockingGroup.getBlockingKey());
          continue;
        }
      }
      Set<RecordPair> pairs = new HashSet<>();
      crossProduct.block(blockingGroup.getGroups()).forEach(rp -> {
        String rpId = rp.getPairId();
        if (!recordIdPairs.contains(rpId)) {
          recordIdPairs.add(rpId);
          pairs.add(rp);
        }
      });
      dataSet.addRecordPairs(pairs);
      c++;
      if (c % 10000 == 0) {
        logger.debug("Processed blocking groups: " + c + "/" + all);
      }
    }
    logger.info("Number of pairs after blocking: " + dataSet.getRecordPairCount());
    dataSet.cleanRecordPairs();
  }

  public void compareRecordPairs(Collection<RecordPair> pairs) {
    pairs = pairs.stream().map(linker::compare).collect(Collectors.toList());
    dataSet.updateRecordPairs(pairs);
  }

  public void compareAndClassifyActiveRecordPairs() {
    logger.info("Getting active record pairs");
    Collection<RecordPair> activeRecordPairs = dataSet.getActiveRecordPairs();
    compareAndClassify(activeRecordPairs);
  }

  public void reclassifyRecordPairs() {
    Collection<RecordPair> pairs = dataSet.getRecordPairs();
    reclassifyRecordPairs(pairs);
    dataSet.updateRecordPairs(pairs);
  }

  public void reclassifyRecordPairs(Collection<RecordPair> recordPairs) {
    Collection<RecordPair> changedPairs = recordPairs.stream()
      .filter(pair -> {
        String previousOutcome = getOutComeFingerPrint(pair);
        pair = linker.classify(pair);
        String newOutcome = getOutComeFingerPrint(pair);
        boolean changed = !previousOutcome.equals(newOutcome);
        if (changed) {
          pair.getTags().add(Tag.create(CHANGED_BY_RECLASSIFICATION, "true", 1.0));
        }
        return changed;
      })
      .collect(Collectors.toList());
    logger.info("Replacing " + changedPairs.size() + " changed pairs");
    dataSet.replaceRecordPairs(changedPairs);
  }

  private static String getOutComeFingerPrint(RecordPair pair) {
    return pair.getClassification() +
      pair.getTags().stream().filter(tag -> tag.getTag().equals(Classifier.TAG_PROBABILITY))
        .map(Tag::getStringValue).findFirst().orElse("NaN");
  }

  private void compareAndClassify(Collection<RecordPair> inputPairs) {
    logger.info("Comparing and classifying {} record pairs", inputPairs.size());
    List<RecordPair> pairs = inputPairs.stream()
      .map(pair -> {
        final Optional<RecordPair> optionalRecordPair = linker.compareAndClassify(pair);
        if (optionalRecordPair.isEmpty()) {
          pair.addTag(TAG_REMOVED_BY_CLASSIFIER);
          return pair;
        }
        return optionalRecordPair.get();
      })
      .collect(Collectors.toList());
    logger.info("Updating {} record pairs", pairs.size());
    dataSet.updateRecordPairs(pairs);
  }

  public void runPostProcessing() {
    logger.info("Running postprocessing");
    Collection<RecordPair> recordPairs = dataSet.getActiveRecordPairs().stream()
      .peek(rp -> rp.addTag(TAG_REMOVED_BY_POSTPROCESSING))
      .collect(Collectors.toList());
    Collection<RecordPair> cleanedPairs =
      DefaultBatchMatcher.postprocess(linksPostprocessor, recordPairs).stream()
        .peek(rp -> rp.removeTag(TAG_REMOVED_BY_POSTPROCESSING))
        .collect(Collectors.toList());
    logger.info("Removed {} record pairs by postprocessing ({} - {})",
      recordPairs.size() - cleanedPairs.size(), recordPairs.size(), cleanedPairs.size()
    );
    dataSet.updateRecordPairs(recordPairs);
  }

  public void runClustering() {
    Set<RecordCluster> clusters = clusterer.cluster(dataSet.getActiveRecordPairs());
    dataSet.addRecordClusters(clusters);
  }

  @Override
  public Collection<RecordPair> matchSources(Collection<RecordCluster> sources) {
    // Add records to dataset
    sources.forEach(source -> dataSet.addRecords(source.getRecords()));

    runBlocking();
    compareAndClassifyActiveRecordPairs();

    // TODO return links
    return Collections.emptyList();
  }

  @Override
  public Set<RecordCluster> cluster(Collection<RecordPair> pairs) {
    return clusterer.cluster(pairs);
  }

  @Override
  public Collection<Record> assignGlobalIds(Collection<RecordPair> pairs) {
    return clusterer.assignGlobalId(pairs);
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

  public LinksPostprocessor getLinksPostprocessor() {
    return linksPostprocessor;
  }

  public void setLinksPostprocessor(LinksPostprocessor linksPostprocessor) {
    this.linksPostprocessor = linksPostprocessor;
  }

  public Clusterer getClusterer() {
    return clusterer;
  }

  public void setClusterer(Clusterer clusterer) {
    this.clusterer = clusterer;
  }
}

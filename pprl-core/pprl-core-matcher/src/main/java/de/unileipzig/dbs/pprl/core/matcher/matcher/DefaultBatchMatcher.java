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

import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordCluster;
import de.unileipzig.dbs.pprl.core.matcher.blocking.Blocker;
import de.unileipzig.dbs.pprl.core.matcher.blocking.BlockingGroup;
import de.unileipzig.dbs.pprl.core.matcher.blocking.CrossProduct;
import de.unileipzig.dbs.pprl.core.matcher.clustering.Clusterer;
import de.unileipzig.dbs.pprl.core.matcher.linking.Linker;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import de.unileipzig.dbs.pprl.core.matcher.postprocessing.LinksPostprocessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Batch matcher that includes the following phases:
 * - extract blocking keys from records and group by them
 * - compare, classify and filter records within each group
 * - postprocess links (e.g. remove unlikely links)
 * - cluster links and optionally assign global record ids to each cluster
 */
public class DefaultBatchMatcher implements BatchMatcher {

  private Blocker blocker;
  private Linker linker;
  private LinksPostprocessor linksPostprocessor;
  protected Clusterer clusterer;

  private final static Logger logger = LogManager.getLogger(DefaultBatchMatcher.class);

  public DefaultBatchMatcher(Blocker blocker, Linker linker,
    Clusterer clusterer) {
    this.blocker = blocker;
    this.linker = linker;
    this.clusterer = clusterer;
  }

  protected DefaultBatchMatcher() {
  }

  @Override
  public Collection<RecordPair> matchSources(Collection<RecordCluster> sources) {
    final Collection<BlockingGroup> blockingGroups = blocker.block(sources);
    logger.info("Number of blocking groups: " + blockingGroups.size());

    final CrossProduct crossProduct = new CrossProduct();
    crossProduct.setFilter(pair -> linker.compareAndClassify(pair).isPresent());

    final Map<String, RecordPair> recordPairs = new HashMap<>();
    long c = 0;
    long all = blockingGroups.size();
    for (BlockingGroup blockingGroup : blockingGroups) {
      if (c > 0) {
        int numberOfRecordPairs = getNumberOfRecordsPairs(blockingGroup);
        if (numberOfRecordPairs > 100000) {
          logger.debug(c + ": " + numberOfRecordPairs);
        }
      }
      crossProduct.block(blockingGroup.getGroups()).forEach(rp -> {
        String rpId = rp.getPairId();
        if (!recordPairs.containsKey(rpId)) {
          recordPairs.put(rpId, rp);
        }
      });
      c++;
      if (c % 1000 == 0) {
        logger.debug("Processed blocking groups: " + c + "/" + all);
        logger.debug("RecordPair map size " + recordPairs.size());
      }
    }
    logger.info("Number of pairs after blocking: " + recordPairs.size());

    return postprocess(recordPairs.values());
  }

  private int getNumberOfRecordsPairs(BlockingGroup blockingGroup) {
    return blockingGroup.getGroups().stream().map(group -> group.getRecords().size()).mapToInt(i -> i)
      .reduce((a, b) -> a * b).getAsInt();
  }

  public Collection<RecordPair> postprocess(Collection<RecordPair> pairs) {
    return postprocess(linksPostprocessor, pairs);
  }

  public static Collection<RecordPair> postprocess(LinksPostprocessor linksPostprocessor,
    Collection<RecordPair> pairs) {
    if (linksPostprocessor != null) {
      return linksPostprocessor.clean(pairs);
    } else {
      return pairs;
    }
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

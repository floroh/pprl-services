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
import de.unileipzig.dbs.pprl.core.common.model.impl.MatchGrade;
import de.unileipzig.dbs.pprl.core.matcher.clustering.Clusterer;
import de.unileipzig.dbs.pprl.core.matcher.linking.Linker;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import de.unileipzig.dbs.pprl.core.matcher.postprocessing.LinksPostprocessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Modification of the {@link DefaultBatchMatcher} with the addition of
 * a second filtering step before the clustering.
 * The matching is done in multiple steps:
 * 1) Compute links with {@link MatchGrade} > NON_MATCH
 * 2) Outside of this matcher: Decide on uncertain links (POSSIBLE + PROBABLE) (e.g.
 *    using another matcher or manual review)
 * 3) Filter revised links with higher MatchGrade (e.g. > PROBABLE) and run clustering
 */
public class TwoStepBatchMatcher extends DefaultBatchMatcher {

  private final static Logger logger = LogManager.getLogger(TwoStepBatchMatcher.class);

  private MatchGrade minimalMatchGradeBeforeClustering = MatchGrade.PROBABLE_MATCH;
  private LinksPostprocessor linksPostprocessorBeforeClustering;

  public TwoStepBatchMatcher(Blocker blocker, Linker linker,
    Clusterer clusterer) {
    super(blocker, linker, clusterer);
  }

  private TwoStepBatchMatcher() {
  }

  @Override
  public Set<RecordCluster> cluster(Collection<RecordPair> pairs) {
    List<RecordPair> filteredPairs = filter(pairs);
    return clusterer.cluster(filteredPairs);
  }

  @Override
  public Collection<Record> assignGlobalIds(Collection<RecordPair> pairs) {
    List<RecordPair> filteredPairs = filter(pairs);
    return clusterer.assignGlobalId(filteredPairs);
  }

  public List<RecordPair> filter(Collection<RecordPair> pairs) {
    pairs = DefaultBatchMatcher.postprocess(linksPostprocessorBeforeClustering, pairs);
    List<RecordPair> filteredPairs = pairs.stream()
      .filter(crp -> crp.getClassification().isAtLeast(minimalMatchGradeBeforeClustering))
      .collect(Collectors.toList());
    long numberOfRemovedPairs = pairs.size() - filteredPairs.size();
    logger.info("Removed " + numberOfRemovedPairs + " record pairs before clustering");
    return filteredPairs;
  }

  public MatchGrade getMinimalMatchGradeBeforeClustering() {
    return minimalMatchGradeBeforeClustering;
  }

  public void setMinimalMatchGradeBeforeClustering(MatchGrade minimalMatchGradeBeforeClustering) {
    this.minimalMatchGradeBeforeClustering = minimalMatchGradeBeforeClustering;
  }

  public LinksPostprocessor getLinksPostprocessorBeforeClustering() {
    return linksPostprocessorBeforeClustering;
  }

  public void setLinksPostprocessorBeforeClustering(
    LinksPostprocessor linksPostprocessorBeforeClustering) {
    this.linksPostprocessorBeforeClustering = linksPostprocessorBeforeClustering;
  }
}

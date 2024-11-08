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

package de.unileipzig.dbs.pprl.core.matcher.postprocessing;

import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author mfranke
 */
public final class MaxBoth implements LinksPostprocessor {
  public static final boolean DEFAULT_PARALLEL_EXECUTION = true;

  private boolean parallelExecution = DEFAULT_PARALLEL_EXECUTION;

  public MaxBoth() {
  }

  public MaxBoth(boolean parallelExecution) {
    this.parallelExecution = parallelExecution;
  }

  @Override
  public <P extends RecordPair> Collection<P> clean(Collection<P> recordPairs) {
    recordPairs = recordPairs.stream().peek(RecordPair::sortRecordsBySource).collect(Collectors.toList());

    final Set<P> cleanedMatches =
      this.parallelExecution ? this.cleanParallel(recordPairs) : this.cleanSequential(recordPairs);
    return cleanedMatches;
  }

  private <P extends RecordPair> Set<P> cleanParallel(Collection<P> classifiedMatches) {
    final CandidatePairGrouper<P> grouper = new CandidatePairGrouper<>(classifiedMatches);
    final Map<Record, P> leftNodes = grouper.groupLeftAndGetMaxParallel();
    final Map<Record, P> rightNodes = grouper.groupRightAndGetMaxParallel();
    final Set<P> cleanedMatches = this.intersectionParallel(leftNodes, rightNodes);
    return cleanedMatches;
  }

  private <P extends RecordPair> Set<P> cleanSequential(Collection<P> classifiedMatches) {
    final CandidatePairGrouper<P> grouper = new CandidatePairGrouper<>(classifiedMatches);
    final Map<String, P> leftNodes = grouper.groupLeftAndGetMaxSequential();
    final Map<String, P> rightNodes = grouper.groupRightAndGetMaxSequential();
    final Set<P> cleanedMatches = this.intersectionSequential(leftNodes, rightNodes);
    return cleanedMatches;
  }


  private <P extends RecordPair> Set<P> intersectionParallel(Map<Record, P> leftNodes,
    Map<Record, P> rightNodes) {
    final Set<P> merged = leftNodes.entrySet()
//				.stream()
      .parallelStream().map(Entry::getValue).filter(i -> {
        final P maxCandForRight = rightNodes.get(i.getRightRecord());
        return maxCandForRight.getLeftRecord().equals(i.getLeftRecord());
      }).collect(Collectors.toCollection(HashSet::new));
//    System.out.println("Merged " + merged.size());
    return merged;
  }

  private <P extends RecordPair> Set<P> intersectionSequential(Map<String, P> leftNodes,
    Map<String, P> rightNodes) {
    final Collection<P> left = leftNodes.values();
    final Set<P> result = new HashSet<>();

    for (P cand : left) {
      final String rightRecordId = cand.getRightRecord().getId().getUniqueId();
//			if (rightNodes.containsKey(rightRecordId)){
      final String leftRecordId = rightNodes.get(rightRecordId).getLeftRecord().getId().getUniqueId();
      if (cand.getLeftRecord().getId().equals(leftRecordId)) {
        result.add(cand);
      }
    }
//		}			
    System.out.println("Merged " + result.size());
    return result;
  }

  @Override
  public String toString() {
    return "Max1-both (Symetric Best Match)";
  }
}
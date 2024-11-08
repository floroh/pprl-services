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
import de.unileipzig.dbs.pprl.core.matcher.model.RecordPairComparator;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static de.unileipzig.dbs.pprl.core.common.model.api.RecordPair.Side;

/**
 * @author mfranke
 */
public final class CandidatePairGrouper<P extends RecordPair> {

  private Collection<P> classifiedMatches;

  public CandidatePairGrouper(Collection<P> classifiedMatches) {
    this.classifiedMatches = classifiedMatches;
  }

  // MAX BOTH
  // ===================================================================================================== //
  public Map<Record, P> groupLeftAndGetMaxParallel() {
    return this.groupAndGetMaxParallel(Side.LEFT);
  }

  public Map<Record, P> groupRightAndGetMaxParallel() {
    return this.groupAndGetMaxParallel(Side.RIGHT);
  }

  private Map<Record, P> groupAndGetMaxParallel(Side side) {
    final Map<Record, P> result = this.classifiedMatches
//				.stream()
      .parallelStream().collect(Collectors.groupingByConcurrent(
        cand -> cand.getRecord(side),
//						(left ? P::getRecord : P::getRightRecord),
        Collectors.collectingAndThen(Collectors.maxBy(Comparator.comparing(P::getSimilarity)), Optional::get)
      ));
//    System.out.println("Parallel " + side + ": " + result.size());
    return result;
  }

  // ===================================================================================================== //

  public Map<String, P> groupLeftAndGetMaxSequential() {
    return this.groupAndGetMaxSequential(Side.LEFT);
  }

  public Map<String, P> groupRightAndGetMaxSequential() {
    return this.groupAndGetMaxSequential(Side.RIGHT);
  }

  private Map<String, P> groupAndGetMaxSequential(Side side) {
    final Map<String, P> nodes = new HashMap<>();

    for (final P candidate : this.classifiedMatches) {
      final Record rec = candidate.getRecord(side);
      final String candidateId = rec.getId().getUniqueId();
      if (nodes.containsKey(candidateId)) {
        final P nodeMaximum = nodes.get(candidateId);
        if (Double.compare(nodeMaximum.getSimilarity(), candidate.getSimilarity()) < 0) {
          nodes.put(candidateId, candidate);
        }
      } else {
        nodes.put(candidateId, candidate);
      }
    }

//    System.out.println("Sequential " + side + ": " + nodes.size());
    return nodes;
  }

  // HUNGARIAN
  // ===================================================================================================== //
  public Map<Record, List<P>> groupLeftParallel() {
    return this.groupParallel(Side.LEFT);
  }

  public Map<Record, List<P>> groupRightParallel() {
    return this.groupParallel(Side.RIGHT);
  }

  private Map<Record, List<P>> groupParallel(Side side) {
    final Map<Record, List<P>> result = this.classifiedMatches
//			.stream()
      .parallelStream().collect(Collectors.groupingBy(
        cand -> cand.getRecord(side),
//					(left ? P::getLeftRecord : P::getRightRecord),
        Collectors.toCollection(ArrayList::new)
      ));

    return result;
  }

  public Map<Record, List<Record>> groupLeftAndMapRightRecordsParallel() {
    return this.groupAndMapRecordsParallel(Side.LEFT);
  }

  public Map<Record, List<Record>> groupRightAndMapLeftRecordsParallel() {
    return this.groupAndMapRecordsParallel(Side.RIGHT);
  }

  private Map<Record, List<Record>> groupAndMapRecordsParallel(Side side) {
    final Map<Record, List<Record>> result = this.classifiedMatches
//			.stream()
      .parallelStream().collect(Collectors.groupingBy(
        cand -> cand.getRecord(side),
//					(left ? P::getLeftRecord : P::getRightRecord),
        Collectors.mapping(
          cand -> cand.getRecord(Side.other(side)),
//						(left ? P::getRightRecord : P::getLeftRecord),
          Collectors.toCollection(ArrayList::new)
        )
      ));

    return result;
  }


  // ===================================================================================================== //
  public Map<Record, List<P>> groupLeftSequential() {
    return this.groupSequential(Side.LEFT);
  }

  public Map<Record, List<P>> groupRightSequential() {
    return this.groupSequential(Side.RIGHT);
  }

  private Map<Record, List<P>> groupSequential(Side side) {
    final HashMap<Record, List<P>> nodes = new HashMap<Record, List<P>>();

    for (final P candidate : this.classifiedMatches) {
      final Record rec = candidate.getRecord(side);

      if (nodes.containsKey(rec)) {
        final List<P> candidates = nodes.get(rec);
        candidates.add(candidate);
      } else {
        final List<P> candidates = new ArrayList<P>();
        candidates.add(candidate);
        nodes.put(rec, candidates);
      }
    }

    return nodes;
  }

  public Map<Record, List<Record>> groupLeftAndMapRightRecordsSequential() {
    return this.groupAndMapRecordsSequential(Side.LEFT);
  }

  public Map<Record, List<Record>> groupRightAndMapLeftRecordsSequential() {
    return this.groupAndMapRecordsSequential(Side.RIGHT);
  }

  private Map<Record, List<Record>> groupAndMapRecordsSequential(Side side) {
    final HashMap<Record, List<Record>> nodes = new HashMap<Record, List<Record>>();

    for (final P candidate : this.classifiedMatches) {
      final Record rec = candidate.getRecord(side);
      final Record mappedCandidate = candidate.getRecord(Side.other(side));

      if (nodes.containsKey(rec)) {
        final List<Record> candidates = nodes.get(rec);
        candidates.add(mappedCandidate);
      } else {
        final List<Record> candidates = new ArrayList<Record>();
        candidates.add(mappedCandidate);
        nodes.put(rec, candidates);
      }
    }

    return nodes;
  }


  // STABLE MARRIAGE
  // ===================================================================================================== //
  public Map<Record, List<P>> groupLeftAndSortBySimilarityParallel() {
    return this.groupAndSortBySimilarityParallel(Side.LEFT);
  }

  public Map<Record, List<P>> groupRightAndSortBySimilarityParallel() {
    return this.groupAndSortBySimilarityParallel(Side.RIGHT);
  }

  private Map<Record, List<P>> groupAndSortBySimilarityParallel(Side side) {
    final Map<Record, List<P>> nodes = this.classifiedMatches
//			.stream()
      .parallelStream().collect(Collectors.groupingBy(
        cand -> cand.getRecord(side),
//					(left ? P::getLeftRecord : P::getRightRecord),
        Collectors.collectingAndThen(Collectors.toCollection(ArrayList::new), assignedCandidates -> {
          assignedCandidates.sort(RecordPairComparator.BY_SIMILARITY);
          return assignedCandidates;
        })
      ));
    System.out.println("Parallel " + side + ": " + nodes.size());
    return nodes;
  }

  // ===================================================================================================== //
  public Map<Record, List<P>> groupLeftAndSortBySimilaritySequential() {
    return this.groupAndSortBySimilaritySequential(Side.LEFT);
  }

  public Map<Record, List<P>> groupRightAndSortBySimilaritySequential() {
    return this.groupAndSortBySimilaritySequential(Side.RIGHT);
  }

  private Map<Record, List<P>> groupAndSortBySimilaritySequential(Side side) {
    final Map<Record, List<P>> nodes = this.groupSequential(side);

    for (final Entry<Record, List<P>> entry : nodes.entrySet()) {
      entry.getValue().sort(RecordPairComparator.BY_SIMILARITY);
    }

    System.out.println("Sequential " + side + ": " + nodes.size());
    return nodes;
  }


  // PSEUDO MEASURES
  // ===================================================================================================== //
  public Map<Record, Long> groupLeftAndCountParallel() {
    return this.groupAndCountParallel(Side.LEFT);
  }

  public Map<Record, Long> groupRightAndCountParallel() {
    return this.groupAndCountParallel(Side.RIGHT);
  }

  private Map<Record, Long> groupAndCountParallel(Side side) {
    final Map<Record, Long> nodes = this.classifiedMatches
//			.stream()
      .parallelStream().collect(Collectors.groupingByConcurrent(
        cand -> cand.getRecord(side),
//					(left ? P::getLeftRecord : P::getRightRecord),
        Collectors.counting()
      ));

    System.out.println("Parallel " + side + ": " + nodes.size());
    return nodes;
  }


  public Map<Boolean, Long> groupLeftAndCountLinksByTypeParallel() {
    final Map<Record, Long> left = this.groupLeftAndCountParallel();
    return this.countLinksByTypeParallel(left);
  }

  public Map<Boolean, Long> groupLeftAndCountLinksByTypeParallel(Map<Record, Long> left) {
    return this.countLinksByTypeParallel(left);
  }

  public Map<Boolean, Long> groupRightAndCountLinksByTypeParallel() {
    final Map<Record, Long> right = this.groupRightAndCountParallel();
    return this.countLinksByTypeParallel(right);
  }

  public Map<Boolean, Long> groupRightAndCountLinksByTypeParallel(Map<Record, Long> right) {
    return this.countLinksByTypeParallel(right);
  }

  private Map<Boolean, Long> countLinksByTypeParallel(Map<Record, Long> nodes) {
    final Map<Boolean, Long> countMap = nodes.entrySet().parallelStream().map(rec -> {
      final boolean isOneToOneLink = (rec.getValue() == 1);
      return isOneToOneLink;
    }).collect(Collectors.groupingByConcurrent(Function.identity(), Collectors.counting()));
    return countMap;
  }


  // ===================================================================================================== //
  public Map<Record, Long> groupLeftAndCountSequential() {
    return this.groupAndCountSequential(Side.LEFT);
  }

  public Map<Record, Long> groupRightAndCountSequential() {
    return this.groupAndCountSequential(Side.RIGHT);
  }

  private Map<Record, Long> groupAndCountSequential(Side side) {
    final Map<Record, Long> nodes = new HashMap<Record, Long>();

    for (final P candidate : this.classifiedMatches) {
      final Record rec = candidate.getRecord(side);
      nodes.merge(rec, 1L, Long::sum);
    }

    System.out.println("Sequential " + side + ": " + nodes.size());
    return nodes;
  }


  public Map<Boolean, Long> groupLeftAndCountLinksByTypeSequential() {
    final Map<Record, Long> left = this.groupLeftAndCountSequential();
    return this.countLinksByTypeParallel(left);
  }

  public Map<Boolean, Long> groupLeftAndCountLinksByTypeSequential(Map<Record, Long> left) {
    return this.countLinksByTypeParallel(left);
  }

  public Map<Boolean, Long> groupRightAndCountLinksByTypeSequential() {
    final Map<Record, Long> right = this.groupRightAndCountSequential();
    return this.countLinksByTypeSequential(right);
  }

  public Map<Boolean, Long> groupRightAndCountLinksByTypeSequential(Map<Record, Long> right) {
    return this.countLinksByTypeSequential(right);
  }

  private Map<Boolean, Long> countLinksByTypeSequential(Map<Record, Long> nodes) {
    final Set<Entry<Record, Long>> entrySet = nodes.entrySet();
    final Map<Boolean, Long> countMap = new HashMap<Boolean, Long>();

    for (final Entry<Record, Long> entry : entrySet) {
      final boolean isOneToOneLink = (entry.getValue() == 1);
      countMap.merge(Boolean.valueOf(isOneToOneLink), 1L, Long::sum);
    }

    return countMap;
  }
}
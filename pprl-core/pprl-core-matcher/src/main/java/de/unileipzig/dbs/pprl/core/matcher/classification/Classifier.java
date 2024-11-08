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

package de.unileipzig.dbs.pprl.core.matcher.classification;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import de.unileipzig.dbs.pprl.core.common.model.impl.MatchGrade;
import de.unileipzig.dbs.pprl.core.common.monitoring.Tag;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Assigns a {@link MatchGrade} to a {@link RecordPair}
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property = "@class")
public interface Classifier {

  String TAG_PROBABILITY = "PROBABILITY";

  enum Label {
    TRUE_MATCH,
    TRUE_NON_MATCH
  }

  MatchGrade classify(RecordPair recordPairWithSimilarity);

  default RecordPair addClassification(RecordPair recordPairWithSimilarity) {
    MatchGrade matchGrade = classify(recordPairWithSimilarity);
    return recordPairWithSimilarity.setClassification(matchGrade);
  }

  /**
   * Classify a collection of record pairs
   *
   * @param recordPairsWithSimilarity set of record pairs with similarity
   * @return set of classified record pairs
   */
  default Set<RecordPair> addClassification(
    Collection<RecordPair> recordPairsWithSimilarity) {
    return recordPairsWithSimilarity.stream()
      .map(this::addClassification)
      .collect(Collectors.toCollection(HashSet::new));
  }

  default void addProbabilityTag(RecordPair recordPairWithSimilarity, double probability) {
    recordPairWithSimilarity.addTag(Tag.create(TAG_PROBABILITY,
      String.format(Locale.US, "%.3f", probability), probability)
    );
  }

  default Optional<Double> getProbabilityTag(RecordPair classifiedPair) {
    return classifiedPair.getTags().stream()
      .filter(tag -> tag.getTag().equals(Classifier.TAG_PROBABILITY))
      .map(Tag::getNumericValue).findFirst();
  }

  /**
   * Filter a set of record pairs and return only the ones classified as a (potential) match
   *
   * @param recordPairsWithSimilarity set of record pairs with similarity
   * @param minMatchGrade             minimal required match grade
   * @return set of record pairs that are at
   */
  default Set<RecordPair> filter(Collection<RecordPair> recordPairsWithSimilarity,
    MatchGrade minMatchGrade) {
    return recordPairsWithSimilarity.stream().filter(pair -> classify(pair).isAtLeast(minMatchGrade))
      .collect(Collectors.toCollection(HashSet::new));
  }

  @JsonIgnore
  default String getModelDescription() {
    return toString();
  }

}

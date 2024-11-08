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

package de.unileipzig.dbs.pprl.core.matcher.similarityCalculation.record;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Compare two records
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property = "@class")
@JsonPropertyOrder(alphabetic=true)
public interface RecordSimilarityCalculator {

  double calculateSimilarity(RecordPair recordPair);

  default RecordPair addSimilarity(RecordPair recordPair) {
    double sim = calculateSimilarity(recordPair);
    return recordPair.setSimilarity(sim);
  }

  default List<RecordPair> addSimilarities(Collection<RecordPair> recordPairs) {
    return recordPairs.parallelStream().map(this::addSimilarity).collect(Collectors.toList());
  }
}

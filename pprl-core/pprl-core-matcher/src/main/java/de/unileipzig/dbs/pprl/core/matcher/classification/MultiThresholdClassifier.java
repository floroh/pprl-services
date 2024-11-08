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

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import de.unileipzig.dbs.pprl.core.common.model.impl.MatchGrade;

/**
 * Threshold-based {@link Classifier}
 */
@JsonPropertyOrder({"possibleThreshold", "probableThreshold", "certainThreshold"})
public class MultiThresholdClassifier implements Classifier {
  private double possibleThreshold;
  private double probableThreshold;
  private double certainThreshold;

  public MultiThresholdClassifier(double possibleThreshold, double probableThreshold,
    double certainThreshold) {
    this.possibleThreshold = possibleThreshold;
    this.probableThreshold = probableThreshold;
    this.certainThreshold = certainThreshold;
  }

  private MultiThresholdClassifier() {
  }

  @Override
  public MatchGrade classify(RecordPair recordPairWithSimilarity) {
    double sim = recordPairWithSimilarity.getSimilarity();
    MatchGrade grade;
    double probability;
    if (sim >= certainThreshold) {
      grade = MatchGrade.CERTAIN_MATCH;
      probability = 0.8 + (sim - certainThreshold) / (1.0 - certainThreshold) * 0.2;
    } else if (sim >= probableThreshold) {
      grade = MatchGrade.PROBABLE_MATCH;
      probability = 0.5 + (sim - probableThreshold) / (certainThreshold - probableThreshold) * 0.3;
    } else if (sim >= possibleThreshold) {
      grade = MatchGrade.POSSIBLE_MATCH;
      probability = 0.8 - (sim - possibleThreshold) / (probableThreshold - possibleThreshold) * 0.3;
    } else {
      grade = MatchGrade.NON_MATCH;
      probability = 1.0 - sim / possibleThreshold * 0.2;
    }
    addProbabilityTag(recordPairWithSimilarity, probability);
    return grade;
  }

  public double getPossibleThreshold() {
    return possibleThreshold;
  }

  public void setPossibleThreshold(double possibleThreshold) {
    this.possibleThreshold = possibleThreshold;
  }

  public double getProbableThreshold() {
    return probableThreshold;
  }

  public void setProbableThreshold(double probableThreshold) {
    this.probableThreshold = probableThreshold;
  }

  public double getCertainThreshold() {
    return certainThreshold;
  }

  public void setCertainThreshold(double certainThreshold) {
    this.certainThreshold = certainThreshold;
  }
}
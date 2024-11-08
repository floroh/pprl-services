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

import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import de.unileipzig.dbs.pprl.core.common.model.impl.MatchGrade;

/**
 * Simple {@link Classifier} that uses a single threshold value
 */
public class SingleThresholdClassifier implements Classifier {
  protected double threshold;

  protected boolean useLinearProbabilityFunction = true;
  protected double leftDistance = 0.05;
  protected double rightDistance = 0.10;

  public SingleThresholdClassifier(double threshold) {
    this.threshold = threshold;
  }

  protected SingleThresholdClassifier() {
  }

  @Override
  public MatchGrade classify(RecordPair recordPairWithSimilarity) {
    MatchGrade grade;
    double similarity = recordPairWithSimilarity.getSimilarity();
    double probability = getProbability(similarity);
    if (testSimilarity(similarity, this.threshold)) {
      grade = MatchGrade.CERTAIN_MATCH;
    } else {
      grade = MatchGrade.NON_MATCH;
    }
    addProbabilityTag(recordPairWithSimilarity, probability);
    return grade;
  }

  protected boolean testSimilarity(double similarity, double threshold) {
    return similarity >= threshold;
  }

  private double getProbability(double similarity) {
    if (useLinearProbabilityFunction) {
      return getProbabilityByLinearFunction(similarity);
    } else {
      return getProbabilityByDistanceToThreshold(similarity);
    }
  }

  private double getProbabilityByDistanceToThreshold(double similarity) {
    return Math.abs(similarity - threshold);
  }

  private double getProbabilityByLinearFunction(double similarity) {
    double cur_leftDistance = Math.min(threshold, leftDistance);
    double cur_rightDistance = Math.min(1 - threshold, rightDistance);
    if (similarity >= threshold) {
      return 0.5 * (1 + Math.min(1.0, (similarity - threshold) / cur_rightDistance));
    } else {
      return 0.5 * (1 + Math.min(1.0, (threshold - similarity) / cur_leftDistance));
    }
  }

  public double getThreshold() {
    return threshold;
  }

  public void setThreshold(double threshold) {
    this.threshold = threshold;
  }

  public double getLeftDistance() {
    return leftDistance;
  }

  public void setLeftDistance(double leftDistance) {
    this.leftDistance = leftDistance;
  }

  public double getRightDistance() {
    return rightDistance;
  }

  public void setRightDistance(double rightDistance) {
    this.rightDistance = rightDistance;
  }
}
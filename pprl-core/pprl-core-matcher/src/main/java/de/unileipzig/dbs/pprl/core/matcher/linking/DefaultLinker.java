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

package de.unileipzig.dbs.pprl.core.matcher.linking;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import de.unileipzig.dbs.pprl.core.matcher.classification.Classifier;
import de.unileipzig.dbs.pprl.core.common.model.impl.MatchGrade;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import de.unileipzig.dbs.pprl.core.matcher.model.api.LinkageProcessDataSet;
import de.unileipzig.dbs.pprl.core.matcher.similarityCalculation.record.RecordSimilarityCalculator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

@JsonPropertyOrder({"recordSimilarityCalculator", "classifier", "minimalMatchGrade", "recordPairTester"})
public class DefaultLinker implements Linker {
  public static final MatchGrade DEFAULT_MINIMAL_MATCH_GRADE = MatchGrade.PROBABLE_MATCH;

  private RecordSimilarityCalculator recordSimilarityCalculator;
  private Classifier classifier;
  private MatchGrade minimalMatchGrade = DEFAULT_MINIMAL_MATCH_GRADE;
  private RecordPairTester recordPairTester = new MatchGradeBasedTester(MatchGrade.PROBABLE_MATCH);
  @JsonIgnore
  private RecordPairTester minimalMatchGradeTester = new MatchGradeBasedTester(MatchGrade.PROBABLE_MATCH);

  protected static Logger logger = LogManager.getLogger(DefaultLinker.class);

  public DefaultLinker(RecordSimilarityCalculator recordSimilarityCalculator, Classifier classifier) {
    this.recordSimilarityCalculator = recordSimilarityCalculator;
    this.classifier = classifier;
  }

  public DefaultLinker(RecordSimilarityCalculator recordSimilarityCalculator,
    Classifier classifier, RecordPairTester recordPairTester) {
    this.recordSimilarityCalculator = recordSimilarityCalculator;
    this.classifier = classifier;
    this.recordPairTester = recordPairTester;
  }

  private DefaultLinker() {
  }

  @Override
  public Optional<RecordPair> compareAndClassify(RecordPair recordPair) {
    RecordPair rp = compare(recordPair);
    rp = classify(rp);
    if (recordPairTester != null) {
      Optional<RecordPair> optionalRecordPair = recordPairTester.collect(rp);
      if (optionalRecordPair.isEmpty()) {
        return minimalMatchGradeTester.collect(recordPair);
      } else {
        return optionalRecordPair;
      }
    }
    return Optional.of(rp);
  }

  @Override
  public RecordPair compare(RecordPair recordPair) {
    return recordSimilarityCalculator.addSimilarity(recordPair);
  }

  @Override
  public RecordPair classify(RecordPair rp) {
    rp = classifier.addClassification(rp);
    if (!rp.getClassification().isAtLeast(minimalMatchGrade)) {
      rp.addTag(LinkageProcessDataSet.TAG_REMOVED_BY_CLASSIFIER);
    }
    return rp;
  }

  public RecordSimilarityCalculator getRecordSimilarityCalculator() {
    return recordSimilarityCalculator;
  }

  public void setRecordSimilarityCalculator(
    RecordSimilarityCalculator recordSimilarityCalculator) {
    this.recordSimilarityCalculator = recordSimilarityCalculator;
  }

  public Classifier getClassifier() {
    return classifier;
  }

  public void setClassifier(Classifier classifier) {
    this.classifier = classifier;
  }

  public MatchGrade getMinimalMatchGrade() {
    return minimalMatchGrade;
  }

  public void setMinimalMatchGrade(MatchGrade minimalMatchGrade) {
    this.minimalMatchGrade = minimalMatchGrade;
  }

  public RecordPairTester getRecordPairTester() {
    return recordPairTester;
  }

  public void setRecordPairTester(RecordPairTester recordPairTester) {
    this.recordPairTester = recordPairTester;
  }
}

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

package de.unileipzig.dbs.pprl.core.matcher;

import de.unileipzig.dbs.pprl.core.common.BitSetUtils;
import de.unileipzig.dbs.pprl.core.common.factories.AttributeFactory;
import de.unileipzig.dbs.pprl.core.common.factories.RecordFactory;
import de.unileipzig.dbs.pprl.core.common.factories.RecordIdFactory;
import de.unileipzig.dbs.pprl.core.common.model.impl.BitSetVector;
import de.unileipzig.dbs.pprl.core.common.model.api.BitVector;
import de.unileipzig.dbs.pprl.core.common.model.impl.MatchGrade;
import de.unileipzig.dbs.pprl.core.common.model.impl.PersonalAttributeType;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.encoder.blocking.Equality;
import de.unileipzig.dbs.pprl.core.encoder.blocking.HLSH;
import de.unileipzig.dbs.pprl.core.matcher.blocking.StandardBlocking;
import de.unileipzig.dbs.pprl.core.matcher.classification.Classifier;
import de.unileipzig.dbs.pprl.core.matcher.classification.MultiThresholdClassifier;
import de.unileipzig.dbs.pprl.core.matcher.classification.SingleThresholdClassifier;
import de.unileipzig.dbs.pprl.core.matcher.clustering.ConnectedComponents;
import de.unileipzig.dbs.pprl.core.matcher.linking.DefaultLinker;
import de.unileipzig.dbs.pprl.core.matcher.linking.MatchGradeBasedTester;
import de.unileipzig.dbs.pprl.core.matcher.linking.RecordPairTester;
import de.unileipzig.dbs.pprl.core.matcher.matcher.DefaultBatchMatcher;
import de.unileipzig.dbs.pprl.core.matcher.matcher.BatchMatcher;
import de.unileipzig.dbs.pprl.core.matcher.matcher.SingletonIncrementalMatcher;
import de.unileipzig.dbs.pprl.core.matcher.matcher.IncrementalMatcher;
import de.unileipzig.dbs.pprl.core.matcher.postprocessing.MaxBoth;
import de.unileipzig.dbs.pprl.core.matcher.similarityCalculation.aggregation.DefaultSimilarityAggregator;
import de.unileipzig.dbs.pprl.core.matcher.similarityCalculation.attribute.BitVectorSimilarityCalculator;
import de.unileipzig.dbs.pprl.core.matcher.similarityCalculation.attribute.StringSimilarityCalculator;
import de.unileipzig.dbs.pprl.core.matcher.similarityCalculation.record.DefaultRecordSimilarityCalculator;
import de.unileipzig.dbs.pprl.core.matcher.similarityCalculation.record.RecordSimilarityCalculator;

import java.io.File;
import java.io.IOException;
import java.util.BitSet;

public class MatcherTestBase {

  protected BatchMatcher getFbfMatcherBatch() {
    StandardBlocking blocker = new StandardBlocking();
    blocker.addBlockingKeyExtractor(new Equality("eqDOB", PersonalAttributeType.DATEOFBIRTH.name()));
    blocker.addBlockingKeyExtractor(new Equality("eqFN", PersonalAttributeType.FIRSTNAME.name()));
    blocker.addBlockingKeyExtractor(new Equality("eqLN", PersonalAttributeType.LASTNAME.name()));

    RecordSimilarityCalculator rsc = new DefaultRecordSimilarityCalculator(
      new BitVectorSimilarityCalculator(BitVectorSimilarityCalculator.SimilarityMethod.JACCARD),
      new DefaultSimilarityAggregator(DefaultSimilarityAggregator.AggregationMethod.AVERAGE)
    );
//		Classifier classifier = new SingleThresholdClassifier(0.85);
    Classifier classifier = new MultiThresholdClassifier(0.5, 0.85, 0.95);

    RecordPairTester recordPairTester = new MatchGradeBasedTester(MatchGrade.POSSIBLE_MATCH);
    DefaultLinker linker = new DefaultLinker(rsc, classifier, recordPairTester);
    MaxBoth maxBoth = new MaxBoth();
    DefaultBatchMatcher matcher = new DefaultBatchMatcher(blocker, linker, new ConnectedComponents());
    matcher.setLinksPostprocessor(maxBoth);
    return matcher;
  }

  protected IncrementalMatcher getRbfMatcherIncremental() {
    StandardBlocking blocker = new StandardBlocking();
    blocker.addBlockingKeyExtractor(new HLSH("hlsh", "rbf", 1024, 13579, 3, 10));

    RecordSimilarityCalculator rsc = new DefaultRecordSimilarityCalculator(
      new BitVectorSimilarityCalculator(BitVectorSimilarityCalculator.SimilarityMethod.JACCARD),
      new DefaultSimilarityAggregator(DefaultSimilarityAggregator.AggregationMethod.AVERAGE)
    );
    Classifier classifier = new SingleThresholdClassifier(0.85);

    RecordPairTester recordPairTester = new MatchGradeBasedTester(MatchGrade.POSSIBLE_MATCH);
    DefaultLinker linker = new DefaultLinker(rsc, classifier, recordPairTester);
    SingletonIncrementalMatcher matcher = new SingletonIncrementalMatcher(blocker, linker);
    return matcher;
  }

  protected BatchMatcher getRbfMatcher() {
    StandardBlocking blocker = new StandardBlocking();
    blocker.addBlockingKeyExtractor(new HLSH("hlsh", "rbf", 1024, 13579, 3, 10));

    RecordSimilarityCalculator rsc = new DefaultRecordSimilarityCalculator(
      new BitVectorSimilarityCalculator(BitVectorSimilarityCalculator.SimilarityMethod.JACCARD),
      new DefaultSimilarityAggregator(DefaultSimilarityAggregator.AggregationMethod.AVERAGE)
    );
    Classifier classifier = new SingleThresholdClassifier(0.85);

    RecordPairTester recordPairTester = new MatchGradeBasedTester(MatchGrade.POSSIBLE_MATCH);
    DefaultLinker linker = new DefaultLinker(rsc, classifier, recordPairTester);
    MaxBoth maxBoth = new MaxBoth();
    DefaultBatchMatcher matcher = new DefaultBatchMatcher(blocker, linker, new ConnectedComponents());
    matcher.setLinksPostprocessor(maxBoth);
    return matcher;
  }

  protected BatchMatcher getExamplePlainMatcher() {
    StandardBlocking blocker = new StandardBlocking();
    blocker.addBlockingKeyExtractor(new Equality("eqFN", PersonalAttributeType.FIRSTNAME.name()));
    blocker.addBlockingKeyExtractor(new Equality("eqLN", PersonalAttributeType.LASTNAME.name()));
    blocker.addBlockingKeyExtractor(new Equality("eqPLZ", PersonalAttributeType.PLZ.name()));

    RecordSimilarityCalculator rsc = new DefaultRecordSimilarityCalculator(
      new StringSimilarityCalculator(StringSimilarityCalculator.SimilarityMethod.JAROWINKLER),
      new DefaultSimilarityAggregator(DefaultSimilarityAggregator.AggregationMethod.AVERAGE)
    );
    Classifier classifier = new SingleThresholdClassifier(0.8);

    RecordPairTester recordPairTester = new MatchGradeBasedTester(MatchGrade.POSSIBLE_MATCH);
    DefaultLinker linker = new DefaultLinker(rsc, classifier, recordPairTester);
    DefaultBatchMatcher matcher = new DefaultBatchMatcher(blocker, linker, new ConnectedComponents());
    return matcher;
  }

  protected String getTmpPath(String fileName) throws IOException {
    return File.createTempFile(fileName, "tmp").getAbsolutePath();
  }

  protected String getFullPath(String path) {
    return this.getClass().getResource(path).getFile();
  }

  protected static BitVector getRandomBitVector(int length) {
    return getRandomBitVector(length, 0.5);
  }

  protected static BitVector getRandomBitVector(int length, double fillShare) {
    BitSet bs = BitSetUtils.generateRandomBitSet(length, (int) (length * fillShare));
    return BitSetVector.fromBitSet(bs, length);
  }

  protected static Record getBitVectorRecord() {
    int bvLength = 128;
    Record record = RecordFactory.getEmptyRecord(RecordIdFactory.get("record"))
      .setAttribute(
        PersonalAttributeType.FIRSTNAME.toString(),
        AttributeFactory.getAttribute(getRandomBitVector(bvLength))
      ).setAttribute(
        PersonalAttributeType.LASTNAME.toString(),
        AttributeFactory.getAttribute(getRandomBitVector(bvLength))
      ).setAttribute(
        PersonalAttributeType.DATEOFBIRTH.toString(),
        AttributeFactory.getAttribute(getRandomBitVector(bvLength))
      );
    return record;
  }
}

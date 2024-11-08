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
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.encoder.attribute.AttributeFrequencyEncoderGroup;
import de.unileipzig.dbs.pprl.core.encoder.record.ParallelPlainRecordEncoder;
import de.unileipzig.dbs.pprl.core.matcher.model.AttributePairWithSimilarity;
import de.unileipzig.dbs.pprl.core.matcher.model.NamedAttributePair;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import de.unileipzig.dbs.pprl.core.common.model.impl.RecordPairSimple;
import de.unileipzig.dbs.pprl.core.matcher.similarityCalculation.aggregation.SimilarityAggregator;
import de.unileipzig.dbs.pprl.core.matcher.similarityCalculation.attribute.AttributeSimilarityCalculator;
import de.unileipzig.dbs.pprl.core.matcher.similarityCalculation.missing.MissingSimilarityStrategy;
import de.unileipzig.dbs.pprl.core.matcher.similarityCalculation.missing.NoModification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@JsonPropertyOrder({"missingSimilarityStrategy", "similarityCalculator", "similarityAggregator"})
public class DefaultRecordSimilarityCalculator implements RecordSimilarityCalculator {
  public static final List<String> ATTRIBUTE_IGNORE_NAME_PATTERNS = List.of(
    ParallelPlainRecordEncoder.SUFFIX_PLAIN_RECORD,
    AttributeFrequencyEncoderGroup.SUFFIX_RELATIVE_FREQUENCY,
    AttributeFrequencyEncoderGroup.SUFFIX_RELATIVE_RANK,
    AttributeFrequencyEncoderGroup.SUFFIX_FREQUENCY_LABEL
  );

  private AttributeSimilarityCalculator similarityCalculator;
  private MissingSimilarityStrategy missingSimilarityStrategy = new NoModification();
  private SimilarityAggregator similarityAggregator;

  protected static Logger logger = LogManager.getLogger(DefaultRecordSimilarityCalculator.class);

  public DefaultRecordSimilarityCalculator(
    AttributeSimilarityCalculator similarityCalculator,
    MissingSimilarityStrategy missingSimilarityStrategy,
    SimilarityAggregator similarityAggregator) {
    this(similarityCalculator, similarityAggregator);
    this.missingSimilarityStrategy = missingSimilarityStrategy;
  }

  public DefaultRecordSimilarityCalculator(AttributeSimilarityCalculator similarityCalculator,
    SimilarityAggregator similarityAggregator) {
    this.similarityCalculator = similarityCalculator;
    this.similarityAggregator = similarityAggregator;
  }

  protected DefaultRecordSimilarityCalculator() {
  }

  @Override
  public double calculateSimilarity(RecordPair recordPair) {
    //TODO Warum diese Kopie? Wird das Paar modifziert?
    RecordPair rpCopy = new RecordPairSimple(recordPair.getLeftRecord(), recordPair.getRightRecord());
    rpCopy = addSimilarity(rpCopy);
    return rpCopy.getSimilarity();
  }

  @Override
  public RecordPair addSimilarity(RecordPair recordPair) {
    Set<NamedAttributePair> attrPairs = getAttributePairs(recordPair);
    Set<AttributePairWithSimilarity> attrSimilarities = getAttributePairWithSimilarities(attrPairs);
    Double sim = similarityAggregator.aggregate(attrSimilarities);
    return buildRecordPairSimilarities(recordPair, sim, attrSimilarities);
  }

  protected Set<AttributePairWithSimilarity> getAttributePairWithSimilarities(
    Set<NamedAttributePair> attrPairs) {
    return attrPairs.stream()
      .map(similarityCalculator::addSimilarity)
      .map(missingSimilarityStrategy::modify)
      .collect(Collectors.toSet());
  }

  protected RecordPair buildRecordPairSimilarities(
    RecordPair recordPair,
    Double sim,
    Set<AttributePairWithSimilarity> attrSimilarities) {

    Map<String, Double> attrSimMap = attrSimilarities.stream().collect(
      Collectors.toMap(AttributePairWithSimilarity::getName, AttributePairWithSimilarity::getSimilarity));

    return recordPair
      .setSimilarity(sim)
      .setAttributeSimilarities(attrSimMap);
  }

  protected Set<NamedAttributePair> getAttributePairs(RecordPair recordPair) {
    Set<NamedAttributePair> attrPairs = new HashSet<>();
    Record leftRecord = recordPair.getLeftRecord();
    Record rightRecord = recordPair.getRightRecord();
    Collection<String> rightAttrNames = rightRecord.getAttributeNames();
    for (String rightAttrName : rightAttrNames) {
      if (ATTRIBUTE_IGNORE_NAME_PATTERNS.stream().anyMatch(rightAttrName::contains)) {
        continue;
      }
      Optional<Attribute> optionalLeftAttribute = leftRecord.getAttribute(rightAttrName);
      if (optionalLeftAttribute.isPresent()) {
        Attribute leftAttr = optionalLeftAttribute.get();
        Attribute rightAttr = rightRecord.getAttribute(rightAttrName).get();
        attrPairs.add(new NamedAttributePair(rightAttrName, leftAttr, rightAttr));
      }
    }
    return attrPairs;
  }

  public AttributeSimilarityCalculator getSimilarityCalculator() {
    return similarityCalculator;
  }

  public MissingSimilarityStrategy getMissingSimilarityStrategy() {
    return missingSimilarityStrategy;
  }

  public SimilarityAggregator getSimilarityAggregator() {
    return similarityAggregator;
  }
}

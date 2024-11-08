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

import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.matcher.model.AttributePair;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import de.unileipzig.dbs.pprl.core.matcher.similarityCalculation.attribute.AttributeSimilarityCalculator;

import java.util.Optional;

/**
 * Simple record comparator that compares only a single attribute, e.g. a record-level bloom filter.
 */
public class SingleAttributeRecordSimilarityCalculator implements RecordSimilarityCalculator {
  private AttributeSimilarityCalculator similarityCalculator;

  private String attributeName;

  public SingleAttributeRecordSimilarityCalculator(AttributeSimilarityCalculator similarityCalculator, String attributeName) {
    this.similarityCalculator = similarityCalculator;
    this.attributeName = attributeName;
  }

  private SingleAttributeRecordSimilarityCalculator() {
  }

  @Override
  public double calculateSimilarity(RecordPair recordPair) {
    Optional<AttributePair> optAttrPair = getAttributePair(recordPair);
    if (optAttrPair.isEmpty()) {
      throw new RuntimeException("Missing attribute: " + attributeName);
    }
    return similarityCalculator.calculateSimilarity(optAttrPair.get());
  }

  public AttributeSimilarityCalculator getSimilarityCalculator() {
    return similarityCalculator;
  }

  public String getAttributeName() {
    return attributeName;
  }

  private Optional<AttributePair> getAttributePair(RecordPair recordPair) {
    Record leftRecord = recordPair.getLeftRecord();
    Record rightRecord = recordPair.getRightRecord();
    Optional<Attribute> leftAttr = leftRecord.getAttribute(attributeName);
    Optional<Attribute> rightAttr = rightRecord.getAttribute(attributeName);
    if (leftAttr.isEmpty() || rightAttr.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(new AttributePair(leftAttr.get(), rightAttr.get()));
  }
}

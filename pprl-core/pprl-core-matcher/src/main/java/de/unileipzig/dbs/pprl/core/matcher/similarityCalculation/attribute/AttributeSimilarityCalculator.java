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

package de.unileipzig.dbs.pprl.core.matcher.similarityCalculation.attribute;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.matcher.model.AttributePair;
import de.unileipzig.dbs.pprl.core.matcher.model.AttributePairWithSimilarity;
import de.unileipzig.dbs.pprl.core.matcher.model.NamedAttributePair;
import de.unileipzig.dbs.pprl.core.matcher.similarityCalculation.missing.MissingSimilarityStrategy;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Compare two attributes
 * If both attributes are missing
 * {@link MissingSimilarityStrategy#MISSING_SIMILARITY} is returned
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property = "@class")
public interface AttributeSimilarityCalculator {

  double calculateSimilarity(AttributePair attributePair);

  default AttributePairWithSimilarity addSimilarity(NamedAttributePair attributePair) {
    double sim = calculateSimilarity(attributePair);
    if (attributePair instanceof AttributePairWithSimilarity) {
      ((AttributePairWithSimilarity) attributePair).setSimilarity(sim);
      return (AttributePairWithSimilarity) attributePair;
    }
    return new AttributePairWithSimilarity(attributePair, sim);
  }

  default List<AttributePairWithSimilarity> addSimilarities(Collection<NamedAttributePair> attributePairs) {
    return attributePairs.stream().map(this::addSimilarity).collect(Collectors.toList());
  }

  default <T> T getAttributeValue(Attribute attribute, Class<T> c) {
    return attribute.getAs(c);
  }
}
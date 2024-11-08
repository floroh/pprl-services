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

import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.matcher.model.AttributePair;
import de.unileipzig.dbs.pprl.core.matcher.similarityCalculation.missing.MissingSimilarityStrategy;

public class EqualityTest implements AttributeSimilarityCalculator {

  @Override
  public double calculateSimilarity(AttributePair attributePair) {
    Attribute left = attributePair.getLeftAttribute();
    Attribute right = attributePair.getRightAttribute();
    if (left.isEmpty() && right.isEmpty()) {
      return MissingSimilarityStrategy.MISSING_SIMILARITY;
    }
    if (left.getObject().equals(right.getObject())) {
      return 1.0;
    }
    return 0;
  }
}

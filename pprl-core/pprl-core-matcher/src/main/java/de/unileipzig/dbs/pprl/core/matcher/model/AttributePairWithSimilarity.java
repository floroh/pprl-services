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

package de.unileipzig.dbs.pprl.core.matcher.model;

import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;

public class AttributePairWithSimilarity extends NamedAttributePair {
  private double similarity;

  public AttributePairWithSimilarity(NamedAttributePair attributePair, Double similarity) {
    this(attributePair.getName(), attributePair.getLeftAttribute(), attributePair.getRightAttribute(),
      similarity
    );
  }

  public AttributePairWithSimilarity(String name, Attribute leftAttribute, Attribute rightAttribute,
    Double similarity) {
    super(name, leftAttribute, rightAttribute);
    this.similarity = similarity;
  }

  public double getSimilarity() {
    return similarity;
  }

  public void setSimilarity(double similarity) {
    this.similarity = similarity;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("AttributePairWithSimilarity [name=");
    builder.append(getName());
    builder.append(", similarity=");
    builder.append(similarity);
    builder.append(", leftAttribute=");
    builder.append(leftAttribute);
    builder.append(", rightAttribute=");
    builder.append(rightAttribute);
    builder.append("]");
    return builder.toString();
  }
}
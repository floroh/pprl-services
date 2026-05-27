/*
 * Copyright © 2018 - 2020 Leipzig University (Database Research Group)
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

package de.unileipzig.dbs.pprl.core.common.model.impl;

import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;

import java.util.Objects;

public class AttributePairWithTruth extends AttributePair {
  private final boolean isMatch;
  private final String matchType;

  public AttributePairWithTruth(Attribute leftAttribute, Attribute rightAttribute, String matchType) {
    super(leftAttribute, rightAttribute);
    this.matchType = matchType;
    this.isMatch = !matchType.contains("NON");
  }

  public String getMatchType() {
    return matchType;
  }

  public boolean isMatch() {
    return isMatch;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    AttributePairWithTruth that = (AttributePairWithTruth) o;
    return matchType == that.matchType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), matchType);
  }

  @Override
  public String toString() {
    return "AttributePairWithTruth{" +
      "matchType=" + matchType +
      ", leftAttribute=" + leftAttribute +
      ", rightAttribute=" + rightAttribute +
      '}';
  }
}

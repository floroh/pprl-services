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

public class AttributePair {
  protected Attribute leftAttribute;
  protected Attribute rightAttribute;

  public AttributePair(Attribute leftAttribute, Attribute rightAttribute) {
    this.leftAttribute = leftAttribute;
    this.rightAttribute = rightAttribute;
  }

  public Attribute getLeftAttribute() {
    return leftAttribute;
  }

  public void setLeftAttribute(Attribute leftAttribute) {
    this.leftAttribute = leftAttribute;
  }

  public Attribute getRightAttribute() {
    return rightAttribute;
  }

  public void setRightAttribute(Attribute rightAttribute) {
    this.rightAttribute = rightAttribute;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("AttributePair [leftAttribute=");
    builder.append(leftAttribute);
    builder.append(", rightAttribute=");
    builder.append(rightAttribute);
    builder.append("]");
    return builder.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((leftAttribute == null) ? 0 : leftAttribute.hashCode());
    result = prime * result + ((rightAttribute == null) ? 0 : rightAttribute.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof AttributePair)) {
      return false;
    }
    AttributePair other = (AttributePair) obj;
    if (leftAttribute == null) {
      if (other.leftAttribute != null) {
        return false;
      }
    } else if (!leftAttribute.equals(other.leftAttribute)) {
      return false;
    }
    if (rightAttribute == null) {
      if (other.rightAttribute != null) {
        return false;
      }
    } else if (!rightAttribute.equals(other.rightAttribute)) {
      return false;
    }
    return true;
  }

}
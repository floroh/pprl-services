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

public class NamedAttributePair extends AttributePair {

  private String name;

  public NamedAttributePair(String name, Attribute leftAttribute, Attribute rightAttribute) {
    super(leftAttribute, rightAttribute);
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("NamedAttributePair [name=");
    builder.append(name);
    builder.append(", leftAttribute=");
    builder.append(leftAttribute);
    builder.append(", rightAttribute=");
    builder.append(rightAttribute);
    builder.append("]");
    return builder.toString();
  }
}

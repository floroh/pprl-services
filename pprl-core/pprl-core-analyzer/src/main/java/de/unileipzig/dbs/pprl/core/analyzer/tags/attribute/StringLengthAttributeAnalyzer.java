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

package de.unileipzig.dbs.pprl.core.analyzer.tags.attribute;

import de.unileipzig.dbs.pprl.core.common.monitoring.Tag;

import java.util.Collections;
import java.util.List;

public class StringLengthAttributeAnalyzer implements AttributeAnalyzer<String> {

  public static final String LEN = "LEN";

  @Override
  public List<Tag> getTags(String attrName, String attr) {
    return Collections.singletonList(Tag.create(attrName, LEN, String.valueOf(getLength(attr)),
      (double) getLength(attr)
    ));
  }

  public static int getLength(String attr) {
    return attr.length();
  }


}

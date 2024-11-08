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

package de.unileipzig.dbs.pprl.core.encoder.attribute;

import de.unileipzig.dbs.pprl.core.common.HashUtils;
import de.unileipzig.dbs.pprl.core.common.model.api.BitVector;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Merge multiple {@link BitVector}s into one using the union operation
 */
public class HashUnion implements AttributeMerger<String> {

  @Override
  public String merge(Map<String, String> attributes) {
    String mergedAttribute = null;
    for (String curAttribute : attributes.values()) {
      if (mergedAttribute == null) {
        mergedAttribute = curAttribute;
      } else {
        mergedAttribute =
          new String(HashUtils.getHMacBytes(mergedAttribute, curAttribute), StandardCharsets.UTF_8);
      }
    }
    return mergedAttribute;
  }

  @Override
  public String toString() {
    return "HashUnion";
  }
}

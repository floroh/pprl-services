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

package de.unileipzig.dbs.pprl.core.encoder.feature;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class UniPosGrams implements FeatureExtractor<String, String> {

  @Override
  public Collection<String> extract(String attributeValue) {
    final Set<String> tokens = new HashSet<>();
    for (int i = 0; i < attributeValue.length(); i++) {
      char c = attributeValue.charAt(i);
      for (int p = 0; p <= 2; p++) {
        tokens.add(c + String.valueOf(i + p));
      }
    }
    return tokens;
  }
}

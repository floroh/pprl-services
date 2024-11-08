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

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class DateTokenizer implements FeatureExtractor<String, Integer> {

  @Override
  public Set<Integer> extract(String attributeValue) {
    Set<Integer> features = Arrays.stream(attributeValue.split("-"))
      .map(Integer::parseInt)
      .collect(Collectors.toSet());
    return features;
  }
}

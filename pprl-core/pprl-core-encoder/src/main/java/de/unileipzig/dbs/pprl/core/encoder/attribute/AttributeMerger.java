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

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.unileipzig.dbs.pprl.core.common.HelperUtils;
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Merges multiple {@link Attribute}s into one
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property = "@class")
public interface AttributeMerger<T> {

  T merge(Map<String, T> attributes);

  default List<T> mergeList(Map<String, List<T>> attributes) {
    return HelperUtils.mapOfListsToListOfMaps(attributes)
      .stream()
      .map(this::merge)
      .collect(Collectors.toList());
  }
}

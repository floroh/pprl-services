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

package de.unileipzig.dbs.pprl.core.common.preprocessing;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.unileipzig.dbs.pprl.core.common.factories.AttributeFactory;
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Preprocess a single attribute value.
 * The result can be of the same type (e.g. a String normalizer)
 * or a different type (e.g. an Integer parser)
 *
 * @param <I> type of the input attribute
 * @param <O> type of the output attribute
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property = "@class")
public interface AttributePreprocessor<I, O> {

  O preprocess(I value);

  default Attribute preprocess(Attribute in) {
    return AttributeFactory.getAttribute(preprocess(in.getAs(getInputClass())));
  }

  default List<O> preprocess(List<I> values) {
    return values.stream()
      .map(this::preprocess)
      .collect(Collectors.toList());
  }

  @JsonIgnore
  Class<I> getInputClass();

  @JsonIgnore
  Class<O> getOutputClass();
}

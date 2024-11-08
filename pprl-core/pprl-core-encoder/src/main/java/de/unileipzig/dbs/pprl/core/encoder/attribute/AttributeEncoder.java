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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.unileipzig.dbs.pprl.core.common.factories.AttributeFactory;
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.ListAttribute;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Encodes a plain {@link Attribute} (e.g. a string) to another attribute (e.g. a bitvector)
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property = "@class")
public interface AttributeEncoder<I, O> {

  default List<O> encode(ListAttribute listAttribute) {
    List<I> attrValues = listAttribute.getListAs(getInputClass());
    return attrValues.stream()
      .map(this::encode)
      .collect(Collectors.toList());
  }

  default O encode(Attribute attribute) {
    I attrValue = attribute.getAs(getInputClass());
    return encode(attrValue);
  }

  @JsonIgnore
  default Attribute getEmptyInputAttribute() {
    if (getInputClass().equals(String.class)) {
      return AttributeFactory.getAttribute("");
    }
    return AttributeFactory.getAttribute(null);
  }

  O encode(I attribute);

  @JsonIgnore
  Class<I> getInputClass();

  @JsonIgnore
  Class<O> getOutputClass();

  String getId();
}

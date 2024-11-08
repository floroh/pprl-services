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
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.encoder.hardening.Hardener;
import de.unileipzig.dbs.pprl.core.common.preprocessing.RecordPreprocessor;
import de.unileipzig.dbs.pprl.core.encoder.model.NamedAttribute;

import java.util.List;

/**
 * Encodes (parts of a) {@link Record} into an {@link Attribute}
 * using one more more {@link AttributeEncoder} mapped to specific attribute ids
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property = "@class")
public interface AttributeEncoderGroup<T> {

  AttributeEncoderGroup<T> addRecordPreprocessor(RecordPreprocessor recordPreprocessor);

  AttributeEncoderGroup<T> addAttributeEncoder(String attributeId, AttributeEncoder<?, T> attributeEncoder);

  AttributeEncoderGroup<T> addHardener(Hardener<T> hardener);

  default List<NamedAttribute> encode(Record record) {
    return List.of(encodeToSingleAttribute(record));
  }

  NamedAttribute encodeToSingleAttribute(Record record);
}

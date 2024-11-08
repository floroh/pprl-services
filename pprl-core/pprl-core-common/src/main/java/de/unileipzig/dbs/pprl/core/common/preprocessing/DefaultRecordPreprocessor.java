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

import de.unileipzig.dbs.pprl.core.common.factories.AttributeFactory;
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.ListAttribute;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;

import java.util.List;

public class DefaultRecordPreprocessor implements RecordPreprocessor {

  private String attributeId;
  private AttributeSplitter attributeSplitter;

  public DefaultRecordPreprocessor(String attributeId, AttributeSplitter attributeSplitter) {
    this.attributeId = attributeId;
    this.attributeSplitter = attributeSplitter;
  }

  private DefaultRecordPreprocessor() {
  }

  @Override
  public Record preprocess(Record record) {
    Attribute attribute = record.getAttribute(attributeId)
      .orElseThrow(() -> new RuntimeException("Missing attribute: " + attributeId));

    if (!attribute.isType(attributeSplitter.getInputClass())) {
      throw new RuntimeException("Wrong attribute type for this preprocessor");
    }

    List<?> out;
    if (attribute instanceof ListAttribute) {
      List<?> vals = ((ListAttribute) attribute).getListAs(attributeSplitter.getInputClass());
      out = attributeSplitter.preprocess(vals);
    } else {
      out = attributeSplitter.preprocess(attribute.getAs(attributeSplitter.getInputClass()));
    }
    Attribute listAttribute = AttributeFactory.getAttribute(out);
    record.setAttribute(attributeId, listAttribute);
    return record;
  }

  public String getAttributeId() {
    return attributeId;
  }

  public AttributeSplitter getAttributeSplitter() {
    return attributeSplitter;
  }
}

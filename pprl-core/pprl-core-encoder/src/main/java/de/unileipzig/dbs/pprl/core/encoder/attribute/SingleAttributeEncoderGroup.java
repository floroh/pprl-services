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

import de.unileipzig.dbs.pprl.core.common.factories.AttributeFactory;
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.ListAttribute;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;

import java.util.List;

public class SingleAttributeEncoderGroup<T> extends AbstractAttributeEncoderGroup<T> {
  private String attributeId;
  private AttributeEncoder<?, T> attributeEncoder;

  public SingleAttributeEncoderGroup(String id, String attributeId, AttributeEncoder<?, T> attributeEncoder) {
    super(id);
    this.attributeId = attributeId;
    this.attributeEncoder = attributeEncoder;
  }

  private SingleAttributeEncoderGroup() {
    super();
  }

  @Override
  public Attribute encodeToAttribute(Record record) {
    Record pRecord = preprocess(record);
    Attribute attribute = pRecord.getAttribute(attributeId)
      .orElse(attributeEncoder.getEmptyInputAttribute());
//      .orElseThrow(() -> new RuntimeException("Missing attribute: " + attributeId));

    if (attribute instanceof ListAttribute) {
      List<T> encodedAttributeParts = encodeListAttribute(attributeEncoder, (ListAttribute) attribute);
      encodedAttributeParts = hardenList(encodedAttributeParts);
      return AttributeFactory.getAttribute(encodedAttributeParts);
    }

    T resultBitVector = encodeAttribute(attributeEncoder, attribute);
    resultBitVector = harden(resultBitVector);
    resultBitVector = keyedHarden(resultBitVector, pRecord);
    return AttributeFactory.getAttribute(resultBitVector);
  }

  @Override
  public SingleAttributeEncoderGroup<T> addAttributeEncoder(String attributeId,
    AttributeEncoder<?, T> attributeEncoder) {
    this.attributeId = attributeId;
    this.attributeEncoder = attributeEncoder;
    return this;
  }

  public String getAttributeId() {
    return attributeId;
  }

  public AttributeEncoder<?, T> getAttributeEncoder() {
    return attributeEncoder;
  }

  private String buildBitVectorId(String attribute, String aeId) {
    return attribute + "_" + aeId;
  }

  @Override
  public String toString() {
    return "SingleAttributeEncoderGroup{" + "id='" + id + '\'' + ", attributeId=" + attributeId +
      ", attributeEncoder=" + attributeEncoder + ", recordPreprocessor=" + recordPreprocessor +
      ", bitVectorHardeners=" + hardeners + '}';
  }
}

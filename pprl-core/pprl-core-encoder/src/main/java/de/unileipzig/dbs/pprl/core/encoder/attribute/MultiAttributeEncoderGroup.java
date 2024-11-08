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
import de.unileipzig.dbs.pprl.core.encoder.hardening.Hardener;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiAttributeEncoderGroup<T> extends AbstractAttributeEncoderGroup<T> {
  private Map<String, AttributeEncoder<?, T>> attributeEncoders;
  private AttributeMerger<T> attributeMerger;

  public MultiAttributeEncoderGroup(String id, AttributeMerger<T> attributeMerger) {
    super(id);
    this.attributeMerger = attributeMerger;
    this.attributeEncoders = new HashMap<>();
  }

  private MultiAttributeEncoderGroup() {
    super();
    this.attributeEncoders = new HashMap<>();
  }

  @Override
  public Attribute encodeToAttribute(Record record) {
    Record pRecord = preprocess(record);

    final Map<String, List<T>> eAttributes = new HashMap<>();
    attributeEncoders.forEach((k, v) -> {
      Attribute attribute = pRecord.getAttribute(k)
        .orElse(v.getEmptyInputAttribute());
//        .orElseThrow(() -> new RuntimeException("Missing attribute: " + k));
      if (attribute instanceof ListAttribute) {
        @SuppressWarnings("unchecked") List<T> resultValues = v.encode((ListAttribute) attribute);
        eAttributes.put(buildEncodedAttributeId(k, v.getId()), resultValues);
      } else {
        T resultValue = encodeAttribute(v, attribute);
        eAttributes.put(buildEncodedAttributeId(k, v.getId()), Collections.singletonList(resultValue));
      }
    });

    boolean containsListAttribute = eAttributes.values()
      .stream()
      .anyMatch(l -> l.size() > 1);
    List<T> mergedValues = attributeMerger.mergeList(eAttributes);

    mergedValues = hardenList(mergedValues);
    mergedValues = keyedHardenList(mergedValues, pRecord);

    if (containsListAttribute) {
      return AttributeFactory.getAttribute(mergedValues);
    }
    return AttributeFactory.getAttribute(mergedValues.getFirst());
  }

  @Override
  public MultiAttributeEncoderGroup<T> addAttributeEncoder(String attributeId,
    AttributeEncoder<?, T> attributeEncoder) {
    attributeEncoders.put(attributeId, attributeEncoder);
    return this;
  }

  @Override
  public MultiAttributeEncoderGroup<T> addHardener(Hardener<T> hardener) {
    hardeners.add(hardener);
    return this;
  }

  public Map<String, AttributeEncoder<?, T>> getAttributeEncoders() {
    return attributeEncoders;
  }

  public AttributeMerger<T> getAttributeMerger() {
    return attributeMerger;
  }

  private String buildEncodedAttributeId(String attribute, String aeId) {
    return attribute + "_" + aeId;
  }

  @Override
  public String toString() {
    return "MultiAttributeEncoderGroup{" + "id='" + id + '\'' + ", attributeEncoders=" + attributeEncoders +
      ", bitVectorMerger=" + attributeMerger + ", bitVectorHardeners=" + hardeners + '}';
  }
}

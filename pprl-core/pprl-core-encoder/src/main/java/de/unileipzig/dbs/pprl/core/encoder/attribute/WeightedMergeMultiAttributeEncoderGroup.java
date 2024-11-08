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
import de.unileipzig.dbs.pprl.core.common.frequencies.WeightCalculator;
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.ListAttribute;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.impl.RecordWithTags;
import de.unileipzig.dbs.pprl.core.encoder.hardening.Hardener;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeightedMergeMultiAttributeEncoderGroup<T> extends AbstractAttributeEncoderGroup<T> {
  private Map<String, AttributeEncoder<?, T>> attributeEncoders;
  private AttributeMerger<T> attributeMerger;

  private WeightCalculator weightCalculator;

  public WeightedMergeMultiAttributeEncoderGroup(String id, AttributeMerger<T> attributeMerger) {
    super(id);
    this.attributeMerger = attributeMerger;
    this.attributeEncoders = new HashMap<>();
  }

  private WeightedMergeMultiAttributeEncoderGroup() {
    super();
    this.attributeEncoders = new HashMap<>();
  }

  @Override
  public Attribute encodeToAttribute(Record record) {
    setupWeightCalculators();

    Record pRecord = preprocess(record);

    final Map<String, List<T>> eAttributes = new HashMap<>();
    attributeEncoders.forEach((k, v) -> {
      Attribute attribute = pRecord.getAttribute(k)
        .orElseThrow(() -> new RuntimeException("Missing attribute: " + k + " (existing: " + pRecord.getAttributeNames() + ")"));
      if (attribute instanceof ListAttribute) {
        throw new NotImplementedException("Weighted encoding is currently not supported for " +
          "list attributes");
      } else {
        if (v instanceof WeightedBitVectorEncoder) {
          WeightedBitVectorEncoder<?, ?> wbve = (WeightedBitVectorEncoder<?, ?>) v;
          wbve.setAttributeName(k);
          wbve.setPlaintextAttribute(record.getAttribute(k).get());
          if (record instanceof RecordWithTags) {
            wbve.provideTags().stream()
              .peek(t -> {
                t.setAttribute(k);
                t.setId0(record.getId().getUniqueId());
              })
              .forEach(t -> ((RecordWithTags) record).getTagTable().addTag(t));
          }
        } else if (v instanceof WeightedTokenBitVectorEncoder) {
          ((WeightedTokenBitVectorEncoder<?, ?>) v).setAttributeName(k);
        }
        T resultValue = encodeAttribute(v, attribute);
        eAttributes.put(buildEncodedAttributeId(k, v.getId()), Collections.singletonList(resultValue));
      }
    });

    boolean containsListAttribute = eAttributes.values()
      .stream()
      .anyMatch(l -> l.size() > 1);
    if (containsListAttribute) {
      throw new NotImplementedException("Weighted merge is currently not supported for list attributes");
    } else if (attributeMerger instanceof WeightedBitVectorUnion) {
      ((WeightedBitVectorUnion) attributeMerger).setCurrentPlainAttributes(record.getAttributes());
    }
    List<T> mergedValues = attributeMerger.mergeList(eAttributes);

    mergedValues = hardenList(mergedValues);
    mergedValues = keyedHardenList(mergedValues, pRecord);

    if (containsListAttribute) {
      return AttributeFactory.getAttribute(mergedValues);
    }
    return AttributeFactory.getAttribute(mergedValues.getFirst());
  }

  private void setupWeightCalculators() {
    if (weightCalculator != null) {
      for (AttributeEncoder<?, T> attributeEncoder : attributeEncoders.values()) {
        if (attributeEncoder instanceof WeightedBitVectorEncoder) {
          ((WeightedBitVectorEncoder<?, ?>) attributeEncoder).setWeightCalculator(weightCalculator);
        } else if (attributeEncoder instanceof WeightedTokenBitVectorEncoder) {
          ((WeightedTokenBitVectorEncoder<?, ?>) attributeEncoder).setWeightCalculator(weightCalculator);
        }
      }
      weightCalculator = null;
    }
  }

  @Override
  public WeightedMergeMultiAttributeEncoderGroup<T> addAttributeEncoder(String attributeId,
    AttributeEncoder<?, T> attributeEncoder) {
    attributeEncoders.put(attributeId, attributeEncoder);
    return this;
  }

  @Override
  public WeightedMergeMultiAttributeEncoderGroup<T> addHardener(Hardener<T> hardener) {
    hardeners.add(hardener);
    return this;
  }

  @Override
  public String getId() {
    return id;
  }

  public Map<String, AttributeEncoder<?, T>> getAttributeEncoders() {
    return attributeEncoders;
  }

  public AttributeMerger<T> getAttributeMerger() {
    return attributeMerger;
  }

  public WeightCalculator getWeightCalculator() {
    return weightCalculator;
  }

  private String buildEncodedAttributeId(String attribute, String aeId) {
    return attribute;
//    return attribute + "_" + aeId;
  }

  @Override
  public String toString() {
    return "MultiAttributeEncoderGroup{" + "id='" + id + '\'' + ", attributeEncoders=" + attributeEncoders +
      ", bitVectorMerger=" + attributeMerger + ", bitVectorHardeners=" + hardeners + '}';
  }
}

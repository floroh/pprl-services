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

package de.unileipzig.dbs.pprl.core.encoder.record;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.unileipzig.dbs.pprl.core.common.factories.RecordFactory;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.impl.RecordWithTags;
import de.unileipzig.dbs.pprl.core.common.monitoring.TagProvider;
import de.unileipzig.dbs.pprl.core.common.monitoring.TagTable;
import de.unileipzig.dbs.pprl.core.encoder.attribute.AttributeEncoderGroup;
import de.unileipzig.dbs.pprl.core.encoder.blocking.BlockingKeyExtractor;
import de.unileipzig.dbs.pprl.core.encoder.crypto.KeyExtractor;
import de.unileipzig.dbs.pprl.core.encoder.crypto.KeyedEncoderComponent;
import de.unileipzig.dbs.pprl.core.encoder.model.NamedAttribute;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DefaultRecordEncoder implements RecordEncoder, TagProvider {

  private final Set<AttributeEncoderGroup> encoderGroups = new HashSet<>();
  private final Set<BlockingKeyExtractor> blockingKeyExtractors = new HashSet<>();

  private TagTable tagTable = new TagTable();

  @Override
  public Record encode(Record record) {
    RecordWithTags workingRecord = new RecordWithTags(
      RecordFactory.getRecordDuplicate(RecordFactory.RecordVariant.DEFAULT, record)
    );
    //TODO Add Record-based preprocessor
    Record encodedRecord = RecordFactory.getEmptyRecord(workingRecord.getId());

    for (AttributeEncoderGroup aeg : encoderGroups) {
      if (aeg instanceof KeyedEncoderComponent) {
        ((KeyedEncoderComponent) aeg).setKey(KeyExtractor.extractKey(record).orElse(null));
      }
      List<NamedAttribute> encodedAttributes = aeg.encode(workingRecord);
      for (NamedAttribute encodedAttribute : encodedAttributes) {
        encodedRecord.setAttribute(encodedAttribute.getId(), encodedAttribute.getAttribute());
      }
    }

//    for (BlockingKeyExtractor bk : blockingKeyExtractors) {
//      Collection<BlockingKey> bkvs = bk.extract(workingRecord);
//      bkvs.forEach(encodedRecord::addBlockingKey);
//    }
    tagTable.append(workingRecord.getTagTable());
    return encodedRecord;
  }

  @Override
  public TagTable provideTagTable() {
    return tagTable;
  }

  public void clearTags() {
    tagTable.clear();
  }

  @Override
  public DefaultRecordEncoder addAttributeEncoderGroup(AttributeEncoderGroup attributeEncoderGroup) {
    encoderGroups.add(attributeEncoderGroup);
    return this;
  }

  @Override
  public DefaultRecordEncoder addBlockingKeyExtractor(BlockingKeyExtractor blockingKeyExtractor) {
    blockingKeyExtractors.add(blockingKeyExtractor);
    return this;
  }

  public Set<AttributeEncoderGroup> getEncoderGroups() {
    return encoderGroups;
  }

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public Set<BlockingKeyExtractor> getBlockingKeyExtractors() {
    return blockingKeyExtractors;
  }

  @Override
  public String toString() {
    return "DefaultRecordEncoder{" + "encoderGroups=" + encoderGroups + ", blockingKeyExtractors=" +
      blockingKeyExtractors + '}';
  }
}

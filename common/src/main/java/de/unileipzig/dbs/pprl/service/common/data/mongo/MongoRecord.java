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

package de.unileipzig.dbs.pprl.service.common.data.mongo;

import de.unileipzig.dbs.pprl.core.common.factories.AttributeFactory;
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordCluster;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordId;
import de.unileipzig.dbs.pprl.core.common.model.impl.RecordIdMap;
import de.unileipzig.dbs.pprl.core.common.serialization.AttributeSerializationType;
import de.unileipzig.dbs.pprl.core.common.serialization.SerializationUtils;
import de.unileipzig.dbs.pprl.service.common.data.dto.EncodingIdDto;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of a {@link Record} for MongoDB.
 */
@Document
public class MongoRecord implements Record {

  public static final String SEPARATOR = "###";

  @Id
  private ObjectId objectId;

  @Indexed
  private int idDataset;

  @Indexed
  private Set<String> properties = new HashSet<>();

  @Indexed
  private EncodingIdDto encodingId;

  private Map<String, String> stringAttributes = new HashMap<>();

  /**
   * Identifier of the record
   */
  private RecordIdMap recordId;

  /**
   * Map storing attributes of the records, e.g. "FIRSTNAME" -> Attribute.of("Peter")
   */
  @Transient
  private Map<String, Attribute> attributes;

  public MongoRecord(int idDataset, RecordId recordId) {
    this();
    this.idDataset = idDataset;
    this.recordId = toRecordIdMap(recordId);
  }

  public MongoRecord(int idDataset, RecordId recordId, Map<String, String> stringAttributes) {
    this();
    this.idDataset = idDataset;
    this.recordId = toRecordIdMap(recordId);
    this.stringAttributes = stringAttributes;
    this.attributes = new HashMap<>();
  }

  private RecordIdMap toRecordIdMap(RecordId recordId) {
    if (recordId instanceof RecordIdMap) {
      return (RecordIdMap) recordId;
    } else {
      RecordIdMap recordIdMap = new RecordIdMap(recordId.getLocalId(), recordId.getSourceId());
      recordId.getIdNames().forEach(name -> recordIdMap.addId(name, recordId.getOptionalId(name).get()));
      return recordIdMap;
    }
  }

  public MongoRecord(int idDataset, ObjectId objectId, Map<String, String> stringAttributes,
    RecordId recordId,
    Map<String, Attribute> attributes) {
    this.idDataset = idDataset;
    this.recordId = toRecordIdMap(recordId);
    this.stringAttributes = stringAttributes;
    this.attributes = attributes;
  }

  private MongoRecord() {
    this.stringAttributes = new HashMap<>();
    this.attributes = new HashMap<>();
  }

  public int getIdDataset() {
    return idDataset;
  }

  public void setIdDataset(int idDataset) {
    this.idDataset = idDataset;
  }

  public ObjectId getObjectId() {
    return objectId;
  }

  public Set<String> getProperties() {
    if (properties == null) {
      return new HashSet<>();
    }
    return properties;
  }

  public void setProperties(Set<String> properties) {
    this.properties = properties;
  }

  public void addProperty(String property) {
    if (properties == null) {
      properties = new HashSet<>();
    }
    properties.add(property);
  }

  public void removeProperty(String property) {
    if (properties != null) {
      properties.remove(property);
    }
  }

  public EncodingIdDto getEncodingId() {
    return encodingId;
  }

  public void setEncodingId(EncodingIdDto encodingId) {
    this.encodingId = encodingId;
  }

  @Override
  public RecordId getId() {
    return recordId;
  }

  @Override
  public void setId(RecordId recordId) {
    this.recordId = toRecordIdMap(recordId);
  }

  @Override
  public Optional<Attribute> getAttribute(String name) {
    if (attributes.containsKey(name)) {
      return Optional.ofNullable(attributes.get(name));
    }
    if (stringAttributes.containsKey(name)) {
      Attribute attribute = deserialize(stringAttributes.get(name));
      attributes.put(name, attribute);
      return Optional.ofNullable(attribute);
    }
    return Optional.empty();
  }

  @Override
  public Record setAttribute(String name, Attribute attribute) {
    attributes.put(name, attribute);
    stringAttributes.put(name, serialize(attribute));
    return this;
  }

  @Override
  public Record removeAttribute(String name) {
    attributes.remove(name);
    stringAttributes.remove(name);
    return this;
  }

  @Override
  public Map<String, Attribute> getAttributes() {
    for (String name : stringAttributes.keySet()) {
      if (!attributes.containsKey(name)) {
        attributes.put(name, deserialize(stringAttributes.get(name)));
      }
    }
    return attributes;
  }

  @Override
  public MongoRecord duplicate() {
    MongoRecord mongoRecord = new MongoRecord(this.idDataset, this.recordId.duplicate(),
      this.stringAttributes.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
    );
    mongoRecord.setEncodingId(new EncodingIdDto(encodingId.getMethod(), encodingId.getProject()));
    return mongoRecord;
  }

  @Override
  public MongoRecordPair getPair(Record other) {
    return new MongoRecordPair(this, (MongoRecord) other);
  }

  @Override
  public RecordCluster getCluster() {
    return new MongoCluster();
  }

  public static String serialize(Attribute attribute) {
    AttributeSerializationType attributeSerializationType =
      SerializationUtils.typeToSerializationType(attribute.getType());
    return attributeSerializationType.ordinal() + SEPARATOR +
      AttributeFactory.attributeToString(
        attributeSerializationType,
        attribute
      );
  }

  public static Attribute deserialize(String attributeString) {
    String[] split = attributeString.split(SEPARATOR);
    String attributeValue = split.length == 1 ? "" : split[1];
    return AttributeFactory.parseAttribute(
      AttributeSerializationType.values()[Integer.parseInt(split[0])],
      attributeValue
    );
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    MongoRecord that = (MongoRecord) o;

    if (idDataset != that.idDataset) {
      return false;
    }
    if (!Objects.equals(stringAttributes, that.stringAttributes)) {
      return false;
    }
    return recordId.equals(that.recordId);
  }

  @Override
  public int hashCode() {
    int result = idDataset;
    result = 31 * result + (stringAttributes != null ? stringAttributes.hashCode() : 0);
    result = 31 * result + recordId.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "MongoRecord{" +
      "objectId=" + objectId +
      ", idDataset=" + idDataset +
      ", recordId=" + recordId +
      ", encodingId=" + encodingId +
      '}';
  }
}

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

package de.unileipzig.dbs.pprl.core.common.model.impl;

import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordCluster;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordId;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Default implementation of a {@link Record}
 */
public class RecordSimple implements Record {
  /**
   * Identifier of the record
   */
  private RecordId id;

  /**
   * Map storing attributes of the records, e.g. "FIRSTNAME" -> Attribute.of("Peter")
   */
  private Map<String, Attribute> attributes;

  public RecordSimple(RecordId id) {
    this.id = id;
    this.attributes = new HashMap<>();
  }

  public RecordSimple(RecordId id, Map<String, Attribute> attributes) {
    this.id = id;
    this.attributes = attributes;
  }

  @Override
  public RecordId getId() {
    return id;
  }

  @Override
  public void setId(RecordId recordId) {
    this.id = recordId;
  }

  @Override
  public Optional<Attribute> getAttribute(String name) {
    return Optional.ofNullable(attributes.get(name));
  }

  @Override
  public Record setAttribute(String name, Attribute attribute) {
    attributes.put(name, attribute);
    return this;
  }

  @Override
  public Record removeAttribute(String name) {
    attributes.remove(name);
    return this;
  }

  @Override
  public Map<String, Attribute> getAttributes() {
    return attributes;
  }

  @Override
  public Record duplicate() {
    return new RecordSimple(this.id.duplicate(), this.attributes.entrySet()
      .stream()
      .collect(Collectors
        .toMap(Map.Entry::getKey, e -> e.getValue()
          .duplicate())));
  }

  @Override
  public RecordPair getPair(Record other) {
    return new RecordPairSimple(this, other);
  }

  @Override
  public RecordCluster getCluster() {
    return new RecordClusterSimple();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RecordSimple record = (RecordSimple) o;
    return id.equals(record.id) && attributes.equals(record.attributes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, attributes);
  }

  @Override
  public String toString() {
    return "RecordImp{" + "id=" + id + ", attributes=" + attributes + '}';
  }
}

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
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of a {@link Record} that is optimised for a small memory footprint
 */
public class RecordLight implements Record {

  private RecordId id;

  private String[] attributeNames;

  private Attribute[] attributeValues;

  private RecordLight() {
  }

  public RecordLight(RecordId id) {
    this.id = id;
  }

  public RecordLight(RecordId id, Map<String, Attribute> attributes) {
    this.id = id;
    this.attributeNames = new String[attributes.size()];
    this.attributeValues = new Attribute[attributes.size()];

    int i = 0;
    for (Map.Entry<String, Attribute> e : attributes.entrySet()) {
      attributeNames[i] = e.getKey();
      attributeValues[i] = e.getValue();
      i = i + 1;
    }
  }

  @Override
  public RecordId getId() {
    return this.id;
  }

  @Override
  public void setId(RecordId recordId) {
    this.id = recordId;
  }

  @Override
  public Optional<Attribute> getAttribute(String name) {
    int pos = Arrays.asList(attributeNames)
      .indexOf(name);
    return pos == -1 ? Optional.empty() : Optional.of(attributeValues[pos]);
  }

  @Override
  public Map<String, Attribute> getAttributes() {
    Map<String, Attribute> attributeMap = new HashMap<>();
    for (int i = 0; i < attributeNames.length; i++) {
      attributeMap.put(attributeNames[i], attributeValues[i]);
    }
    return attributeMap;
  }

  @Override
  public Record setAttribute(String name, Attribute attribute) {
    int index = ArrayUtils.indexOf(attributeNames, name);
    if (index == ArrayUtils.INDEX_NOT_FOUND) {
      attributeNames = ArrayUtils.add(attributeNames, name);
      attributeValues = ArrayUtils.add(attributeValues, attribute);
    } else {
      attributeValues[index] = attribute;
    }
    return this;
  }

  @Override
  public Record removeAttribute(String name) {
    int index = ArrayUtils.indexOf(attributeNames, name);
    if (index == ArrayUtils.INDEX_NOT_FOUND) {
      return this;
    }
    attributeNames = ArrayUtils.remove(attributeNames, index);
    attributeValues = ArrayUtils.remove(attributeValues, index);
    return this;
  }

  @Override
  public Set<String> getAttributeNames() {
    return new HashSet<>(Arrays.asList(attributeNames));
  }

  @Override
  public int getNumberOfAttributes() {
    return attributeNames.length;
  }

  @Override
  public Record duplicate() {
    return new RecordLight(this.id.duplicate(), getAttributes().entrySet()
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

  public RecordLight fromRecord(Record record) {
    return new RecordLight(record.getId(), record.getAttributes());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RecordLight that = (RecordLight) o;
    return Objects.equals(id, that.id) && Arrays.equals(attributeNames, that.attributeNames) &&
      Arrays.equals(attributeValues, that.attributeValues);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(id);
    result = 31 * result + Arrays.hashCode(attributeNames);
    result = 31 * result + Arrays.hashCode(attributeValues);
    return result;
  }

  @Override
  public String toString() {
    return "RecordLight{" + "id=" + id + ", attributeNames=" + Arrays.toString(attributeNames) +
      ", attributeValues=" + Arrays.toString(attributeValues) + '}';
  }
}

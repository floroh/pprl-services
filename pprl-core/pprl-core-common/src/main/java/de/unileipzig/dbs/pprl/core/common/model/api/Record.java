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

package de.unileipzig.dbs.pprl.core.common.model.api;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Represents a record (e.g. of a person) with a unique id ({@link RecordId}) and multiple associated
 * {@link Attribute}s
 * (e.g. name, date of birth, etc.)
 */
public interface Record {

  RecordId getId();

  void setId(RecordId recordId);

  Optional<Attribute> getAttribute(String name);

  Map<String, Attribute> getAttributes();

  Record setAttribute(String name, Attribute attribute);

  Record removeAttribute(String name);

  default void removeAllAttributes() {
    getAttributeNames().forEach(this::removeAttribute);
  }

  /**
   * Get deep clone of this record
   *
   * @return cloned record
   */
  Record duplicate();

  @JsonIgnore
  RecordPair getPair(Record other);

  @JsonIgnore
  RecordCluster getCluster();

  default RecordCluster asCluster() {
    RecordCluster cluster = getCluster();
    cluster.addRecord(this);
    return cluster;
  }

  @JsonIgnore
  default int getNumberOfAttributes() {
    return getAttributeNames().size();
  }

  @JsonIgnore
  default Set<String> getAttributeNames() {
    return new HashSet<>(getAttributes().keySet());
  }
}

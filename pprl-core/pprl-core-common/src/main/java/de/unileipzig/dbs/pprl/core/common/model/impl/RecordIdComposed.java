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

import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordId;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Store multiple identifiers of a {@link Record}
 * - source: id of the data source
 * - local: id of the record in that data source
 * - global: id of the record that is equal between matching records of different sources
 * - unique: id of the record that is unique in the matching context
 */
public class RecordIdComposed implements RecordId {

  public static final String DEFAULT_PREFIX = "rec-";
  public static final String DEFAULT_SEPARATOR = "-";

  public static final String[] SUPPORTED_TYPES = new String[] {LOCAL_ID, SOURCE_ID, GLOBAL_ID};

  private static String separator = DEFAULT_SEPARATOR;
  private static String prefix = DEFAULT_PREFIX;

  private String composedId;

  private RecordIdComposed(String composedId) {
    this.composedId = composedId;
  }

  public RecordIdComposed addId(String name, String value) {
    checkType(name);
    checkValue(value);
    String localId = getLocalId();
    String sourceId = getSourceId();
    Optional<String> globalId = getOptionalId(GLOBAL_ID);
    switch (name) {
      case LOCAL_ID:
        localId = value;
        break;
      case SOURCE_ID:
        sourceId = value;
        break;
      case GLOBAL_ID:
        globalId = Optional.of(value);
    }
    composedId = globalId.isEmpty() ?
      toComposedId(localId, sourceId) : toComposedId(localId, sourceId, globalId.get());
    return this;
  }

  @Override
  public RecordId removeId(String name) {
    checkType(name);
    if (name.equals(GLOBAL_ID)) {
      composedId = toComposedId(getLocalId(), getSourceId());
    } else {
      throw new RuntimeException("Cannot remove id type " + name);
    }
    return this;
  }

  private void checkType(String name) {
    if (!List.of(SUPPORTED_TYPES).contains(name)) {
      throw new RuntimeException("Unsupported id type " + Arrays.toString(SUPPORTED_TYPES) + ": " + name);
    }
  }

  private void checkValue(String value) {
    if (value.contains(separator)) {
      throw new RuntimeException("Id value must not contain " + separator);
    }
  }

  @Override
  public Optional<String> getOptionalId(String name) {
//    checkType(name);
    switch (name) {
      case LOCAL_ID:
        return Optional.of(getLocalId());
      case SOURCE_ID:
        return Optional.of(getSourceId());
      case GLOBAL_ID:
        return getGlobalId();
      case UNIQUE_ID:
        return Optional.of(getUniqueId());
    }
    return Optional.empty();
//    throw new RuntimeException("Cannot be reached due to type check");
  }

  @Override
  public String getLocalId() {
    return getSimpleRecordId().getLocalId();
  }

  @Override
  public String getSourceId() {
    return getSimpleRecordId().getSourceId();
  }

  private Optional<String> getGlobalId() {
    return getSimpleRecordId().getOptionalId(GLOBAL_ID);
  }

  @Override
  public String getUniqueId() {
    return composedId;
  }

  @Override
  public Collection<String> getIdNames() {
    return Arrays.stream(SUPPORTED_TYPES)
      .filter(idName -> getOptionalId(idName).isPresent())
      .collect(Collectors.toList());
  }

  public void clearGlobalId() {
    composedId = toComposedId(getLocalId(), getSourceId());
  }

  public RecordIdComposed duplicate() {
    return new RecordIdComposed(composedId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(composedId);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RecordIdComposed recordId = (RecordIdComposed) o;
    if (getOptionalId(GLOBAL_ID).orElse("1")
      .equals(recordId.getOptionalId(GLOBAL_ID)
        .orElse("2"))) {
      return true;
    }
    return getLocalId().equals(recordId.getLocalId()) && getSourceId().equals(recordId.getSourceId());
  }

  @Override
  public String toString() {
    return composedId;
  }

  private RecordId getSimpleRecordId() {
    String[] parts = StringUtils.removeStart(composedId, prefix).split(separator);
    RecordIdMap id;
    if (parts.length >= 2) {
      id = new RecordIdMap(parts[0]);
      id.addId(SOURCE_ID, parts[1]);
      if (parts.length == 3) {
        id.addId(GLOBAL_ID, parts[2]);
      }
      return id;
    }
    throw new RuntimeException("Ill-formated composedId: " + composedId);
  }

  public static String toComposedId(String localId, String sourceId, String globalId) {
    return toComposedId(localId, sourceId) + separator + globalId;
  }

  public static String toComposedId(String localId, String sourceId) {
    return prefix + localId + separator + sourceId;
  }

  public static RecordIdComposed of(String localId) {
    return new RecordIdComposed(toComposedId(localId, DEFAULT_SOURCE_ID));
  }

  public static RecordIdComposed of(String localId, String sourceId, String globalId) {
    return new RecordIdComposed(toComposedId(localId, sourceId, globalId));
  }

  public static RecordIdComposed of(String localId, String sourceId) {
    return new RecordIdComposed(toComposedId(localId, sourceId));
  }

  public static RecordIdComposed ofComposed(String composedId) {
    return new RecordIdComposed(composedId);
  }
}

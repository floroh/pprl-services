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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Store arbitrary identifiers of a {@link Record}
 */
public class RecordIdMap implements RecordId {
  private Map<String, String> ids;

  public RecordIdMap(String localId) {
    this(localId, DEFAULT_SOURCE_ID);
  }

  public RecordIdMap(String localId, String sourceId) {
    this();
    ids.put(LOCAL_ID, localId);
    ids.put(SOURCE_ID, sourceId);
  }

  private RecordIdMap() {
    ids = new HashMap<>();
  }

  @Override
  public RecordId addId(String name, String value) {
    ids.put(name, value);
    return this;
  }

  @Override
  public RecordId removeId(String name) {
    ids.remove(name);
    return this;
  }

  @Override
  public Optional<String> getOptionalId(String name) {
    return Optional.ofNullable(ids.get(name));
  }

  @Override
  public String getUniqueId() {
    return getOptionalId(UNIQUE_ID).orElse(getUniqueLikeId());
  }

  @Override
  public Collection<String> getIdNames() {
    return ids.keySet();
  }

  @Override
  public RecordId duplicate() {
    RecordIdMap clone = new RecordIdMap();
    ids.forEach(clone::addId);
    return clone;
  }

  public Map<String, String> getIds() {
    return ids;
  }

  public void setIds(Map<String, String> ids) {
    this.ids = ids;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    RecordIdMap that = (RecordIdMap) o;
    if (getOptionalId(GLOBAL_ID).orElse("1")
      .equals(that.getOptionalId(GLOBAL_ID)
        .orElse("2"))) {
      return true;
    }
    return getLocalId().equals(that.getLocalId()) && getSourceId().equals(that.getSourceId());
  }

  @Override
  public int hashCode() {
    return getLocalId().hashCode() + 31 * getSourceId().hashCode();
  }

  @Override
  public String toString() {
    return "RecordIdMap{" +
      "ids=" + ids +
      '}';
  }
}

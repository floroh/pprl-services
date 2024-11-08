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

import de.unileipzig.dbs.pprl.core.common.model.impl.RecordIdComposed;

import java.util.Collection;
import java.util.Optional;

/**
 * Store multiple identifiers of a {@link Record}
 */
public interface RecordId {

  String UNIQUE_ID = "UNIQUE_ID";
  String LOCAL_ID = "LOCAL_ID";
  String SOURCE_ID = "SOURCE_ID";
  String GLOBAL_ID = "GLOBAL_ID";
  String BLOCK_ID = "BLOCK_ID";

  String MISSING_ID = "NaN";

  String DEFAULT_SOURCE_ID = "DEFAULT_SOURCE";

  RecordId addId(String name, String value);

  RecordId removeId(String name);

  Optional<String> getOptionalId(String name);

  String getUniqueId();

  Collection<String> getIdNames();

  default String getId(String name) {
    Optional<String> optId = getOptionalId(name);
    if (optId.isEmpty() && name.equals(SOURCE_ID)) return DEFAULT_SOURCE_ID;
    return optId.orElseThrow(() ->
      new RuntimeException("Trying to get missing record id " + name + " from " + this));
  }

  default String getLocalId() {
    return getId(LOCAL_ID);
  }

  default String getSourceId() {
    return getId(SOURCE_ID);
  }

  default String getUniqueLikeId() {
    return RecordIdComposed.toComposedId(getLocalId(), getSourceId());
  }

  RecordId duplicate();
}

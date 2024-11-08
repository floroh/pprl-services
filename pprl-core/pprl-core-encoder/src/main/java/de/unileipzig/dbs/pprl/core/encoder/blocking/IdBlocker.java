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

package de.unileipzig.dbs.pprl.core.encoder.blocking;

import de.unileipzig.dbs.pprl.core.common.factories.BlockingKeyFactory;
import de.unileipzig.dbs.pprl.core.common.model.api.BlockingKey;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * This BlockingKeyExtractor uses a Record-ID component as Blockingkeys.
 * {@link de.unileipzig.dbs.pprl.core.common.model.api.RecordId}
 */
public class IdBlocker implements BlockingKeyExtractor {

  /**
   * Component id
   **/
  private String id;

  /**
   * Name of the Record-ID
   */
  private String idName;

  public IdBlocker(String id, String idName) {
    this.id = id;
    this.idName = idName;
  }

  protected IdBlocker() {
  }

  @Override
  public Set<BlockingKey> extract(Record record) {
    Set<BlockingKey> blockingKeys = new HashSet<>();
    Optional<String> optionalId = record.getId().getOptionalId(idName);
    if (isAvailable(optionalId)) {
      blockingKeys.add(BlockingKeyFactory.getBlockingKey(id, optionalId.get()));
    }
    return blockingKeys;
  }

  private boolean isAvailable(Optional<String> optionalId) {
    return optionalId.isPresent() && !optionalId.get().isEmpty();
  }

  public String getId() {
    return id;
  }

  public String getIdName() {
    return idName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    IdBlocker that = (IdBlocker) o;

    return new EqualsBuilder().append(id, that.id)
      .append(idName, that.idName)
      .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37).append(id)
      .append(idName)
      .toHashCode();
  }
}

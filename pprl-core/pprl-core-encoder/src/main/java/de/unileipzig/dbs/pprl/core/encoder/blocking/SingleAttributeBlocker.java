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

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.BlockingKey;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@JsonPropertyOrder({"id", "attributeKey"})
public abstract class SingleAttributeBlocker implements BlockingKeyExtractor {
  protected String id;
  protected String attributeKey;

  public SingleAttributeBlocker(String id, String attributeKey) {
    this.id = id;
    this.attributeKey = attributeKey;
  }

  protected SingleAttributeBlocker() {
  }

  @Override
  public Set<BlockingKey> extract(Record record) {
    Set<BlockingKey> blockingKeys = new HashSet<>();
    Optional<Attribute> attribute = record.getAttribute(attributeKey);
    attribute.ifPresent(value -> blockingKeys.addAll(extract(value)));
    return blockingKeys;
  }

  abstract Collection<BlockingKey> extract(Attribute attribute);


  public String getId() {
    return id;
  }

  public String getAttributeKey() {
    return attributeKey;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    SingleAttributeBlocker that = (SingleAttributeBlocker) o;

    return new EqualsBuilder().append(id, that.id)
      .append(attributeKey, that.attributeKey)
      .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37).append(id)
      .append(attributeKey)
      .toHashCode();
  }
}

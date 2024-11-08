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
import de.unileipzig.dbs.pprl.core.common.factories.BlockingKeyFactory;
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.BlockingKey;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.impl.PersonalAttributeType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@JsonPropertyOrder({"id", "attributeKeys"})
public class SoundexMulti implements BlockingKeyExtractor {
  protected String id;
  protected List<String> attributeKeys;

  private List<String> attributesNamesForSoundex = List.of(
    PersonalAttributeType.FIRSTNAME.name(),
    PersonalAttributeType.FIRSTNAME.name() + "_DEV",
    PersonalAttributeType.LASTNAME.name(),
    PersonalAttributeType.LASTNAME.name() + "_DEV"
  );

  public SoundexMulti(String id, String... attributeKeys) {
    this.id = id;
    this.attributeKeys = Arrays.asList(attributeKeys);
  }

  private SoundexMulti() {
  }

  @Override
  public Set<BlockingKey> extract(Record record) {
    Set<BlockingKey> blockingKeys = new HashSet<>();
    StringBuilder sb = new StringBuilder();
    for (String attributeKey : attributeKeys) {
      Optional<Attribute> optionalAttribute = record.getAttribute(attributeKey);
      if (optionalAttribute.isEmpty() || optionalAttribute.get().isEmpty()) {
        return blockingKeys;
      }
      optionalAttribute.ifPresent(
        attribute -> {
          if (attributesNamesForSoundex.contains(attributeKey)) {
            Soundex.encode(attribute.getAsString())
              .ifPresent(
                value -> sb.append("#")
                  .append(value)
              );
          } else {
              sb.append("#").append(attribute.getAsString());
          }
        }
      );
    }
//    Equality.encode(sb.toString()).ifPresent(s -> blockingKeys.add(BlockingKeyFactory.getBlockingKey(id, s)));
    blockingKeys.add(BlockingKeyFactory.getBlockingKey(id, sb.toString()));
    return blockingKeys;
  }


  public String getId() {
    return id;
  }

  public List<String> getAttributeKeys() {
    return attributeKeys;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    SoundexMulti that = (SoundexMulti) o;

    return new EqualsBuilder().append(id, that.id)
      .append(attributeKeys, that.attributeKeys)
      .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37).append(id)
      .append(attributeKeys)
      .toHashCode();
  }
}

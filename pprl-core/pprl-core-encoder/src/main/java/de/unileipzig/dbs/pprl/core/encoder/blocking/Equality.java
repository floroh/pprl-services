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

import de.unileipzig.dbs.pprl.core.common.HashUtils;
import de.unileipzig.dbs.pprl.core.common.factories.BlockingKeyFactory;
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.BlockingKey;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Blocking method
 */
public class Equality extends SingleAttributeBlocker {

  public Equality(String id, String attributeKey) {
    super(id, attributeKey);
  }

  private Equality() {
  }

  @Override
  public Collection<BlockingKey> extract(Attribute attribute) {
    Set<BlockingKey> blockingKeys = new HashSet<>();
    String attributeValue = attribute.getAsString();
    encode(attributeValue).ifPresent(s -> blockingKeys.add(BlockingKeyFactory.getBlockingKey(id, s)));
    return blockingKeys;
  }

  public static Optional<String> encode(String in) {
    return Optional.of(String.valueOf(HashUtils.getSHA(in)));
  }
}

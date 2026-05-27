/*
 * Copyright © 2018 - 2020 Leipzig University (Database Research Group)
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

package de.unileipzig.dbs.pprl.core.analyzer.tags.recordpair;

import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import de.unileipzig.dbs.pprl.core.common.model.impl.PersonalAttributeType;
import de.unileipzig.dbs.pprl.core.common.monitoring.Tag;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public interface RecordPairAnalyzer {

  List<Tag> getTags(RecordPair recordPair);

  default List<String> getAttributeNames(RecordPair recordPair) {
    Set<String> attributeNames = new HashSet<>(recordPair.getLeftRecord().getAttributeNames());
    attributeNames.addAll(recordPair.getRightRecord().getAttributeNames());
    return attributeNames.stream()
      .filter(name -> !List.of(
        PersonalAttributeType.PLACEOFBIRTH.name(),
        PersonalAttributeType.NAMEATBIRTH.name()
      ).contains(name))
      .sorted(new PersonalAttributeType.AttributeNameComparator())
      .collect(Collectors.toList());
  }

  default boolean isEmpty(Optional<Attribute> optionalAttribute) {
    return optionalAttribute.isEmpty() || optionalAttribute.get().isEmpty();
  }
}

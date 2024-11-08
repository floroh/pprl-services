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

package de.unileipzig.dbs.pprl.core.matcher.linking;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Compare {@link Record}s of two datasets and return pairs of matching records
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property = "@class")
public interface Linker {

  Optional<RecordPair> compareAndClassify(RecordPair recordPair);

  RecordPair classify(RecordPair recordPair);
  RecordPair compare(RecordPair recordPair);

  default Set<RecordPair> compareAndClassify(Collection<RecordPair> recordPairs) {
    return recordPairs.stream()
      .map(this::compareAndClassify)
      .flatMap(Optional::stream)
      .collect(Collectors.toSet());
  }

}

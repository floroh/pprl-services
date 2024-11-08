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

package de.unileipzig.dbs.pprl.core.matcher.blocking;

import de.unileipzig.dbs.pprl.core.common.factories.BlockingKeyFactory;
import de.unileipzig.dbs.pprl.core.common.model.api.BlockingKey;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordCluster;

import java.util.Collection;
import java.util.Collections;

/**
 * Create a single {@link BlockingGroup} for all records so that the subsequent process ({@link CrossProduct})
 * generates all possible record pair combinations between the sources
 */
public class FullBlocking implements Blocker {

  @Override
  public Collection<BlockingKey> generateKeys(Record record) {
    return Collections.emptyList();
  }

  @Override
  public Collection<BlockingGroup> block(Collection<RecordCluster> sources) {
    return Collections
      .singleton(new BlockingGroupInMemory(BlockingKeyFactory.getBlockingKey("DUMMY", "DUMMY"), sources));
  }
}

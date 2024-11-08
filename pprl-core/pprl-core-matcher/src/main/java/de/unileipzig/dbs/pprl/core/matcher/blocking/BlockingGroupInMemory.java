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

import de.unileipzig.dbs.pprl.core.common.model.api.BlockingKey;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordCluster;

import java.util.Collection;

public class BlockingGroupInMemory implements BlockingGroup {
  private BlockingKey blockingKey;
  private Collection<RecordCluster> groups;

  public BlockingGroupInMemory(BlockingKey blockingKey, Collection<RecordCluster> groups) {
    this.blockingKey = blockingKey;
    this.groups = groups;
  }

  @Override
  public BlockingKey getBlockingKey() {
    return blockingKey;
  }

  @Override
  public Collection<RecordCluster> getGroups() {
    return groups;
  }
}

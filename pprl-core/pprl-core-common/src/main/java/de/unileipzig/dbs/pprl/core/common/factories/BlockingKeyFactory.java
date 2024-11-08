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

package de.unileipzig.dbs.pprl.core.common.factories;

import de.unileipzig.dbs.pprl.core.common.model.api.BlockingKey;
import de.unileipzig.dbs.pprl.core.common.model.impl.BlockingKeyImpl;

public class BlockingKeyFactory {
  public enum BlockingKeyVariant {DEFAULT}

  public static BlockingKey getBlockingKey(String id, String value) {
    return getBlockingKey(BlockingKeyVariant.DEFAULT, id, value);
  }

  public static BlockingKey getBlockingKey(BlockingKeyVariant variant, String id, String value) {
    switch (variant) {
      default:
      case DEFAULT:
        return new BlockingKeyImpl(id, value);
    }
  }
}

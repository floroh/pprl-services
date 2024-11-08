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

import de.unileipzig.dbs.pprl.core.common.BitSetUtils;
import de.unileipzig.dbs.pprl.core.common.factories.AttributeFactory;
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.impl.BitSetVector;
import de.unileipzig.dbs.pprl.core.common.model.api.BitVector;
import de.unileipzig.dbs.pprl.core.common.model.api.BlockingKey;
import org.junit.jupiter.api.Test;

import java.util.BitSet;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HLSHTest {

  @Test
  void extract() {
    int length = 256;
    BitSet bs = BitSetUtils.generateRandomBitSet(length, length / 4);
    BitVector bv = new BitSetVector(length, bs);
    Attribute attr = AttributeFactory.getAttribute(bv);
    HLSH hlsh = new HLSH("lsh", "vorname", length, 1357, 7, 9);

    Collection<BlockingKey> keys = hlsh.extract(attr);
    assertEquals(7, keys.size());
    for (BlockingKey key : keys) {
      assertEquals(9, key.getValue()
        .length());
    }
  }
}
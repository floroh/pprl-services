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

package de.unileipzig.dbs.pprl.core.encoder.hardening;

import de.unileipzig.dbs.pprl.core.common.BitSetUtils;
import de.unileipzig.dbs.pprl.core.common.model.impl.BitSetVector;
import de.unileipzig.dbs.pprl.core.common.model.api.BitVector;
import org.junit.jupiter.api.Test;

import java.util.BitSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class BalancingTest {

  @Test
  void harden() {

    BitSet bs = BitSetUtils.generateRandomBitSet(32, 7);
    BitVector bv = new BitSetVector(32, bs);

    BitVectorHardener bvHardener = new Balancing(42);
    BitVector bvH = bvHardener.harden(bv);

    BitVectorHardener bvHardenerSame = new Balancing(42);
    BitVector bvHS = bvHardenerSame.harden(bv);

    BitVectorHardener bvHardenerDiff = new Balancing(123);
    BitVector bvHD = bvHardenerDiff.harden(bv);

    assertEquals(2 * bv.getLength(), bvH.getLength());
    assertEquals(bvH.getLength() / 2, bvH.getCardinality());
    assertEquals(bvH, bvHS);
    assertNotEquals(bvH, bvHD);
  }
}
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

package de.unileipzig.dbs.pprl.core.common;

import de.unileipzig.dbs.pprl.core.common.model.impl.BitSetVector;
import de.unileipzig.dbs.pprl.core.common.model.api.BitVector;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.BitSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BitVectorTest {
  private BitVector shortBitVector;

  @BeforeEach
  void setUp() {
    BitSet bs = new BitSet();
    bs.set(2);
    bs.set(20, 25);
    shortBitVector = BitSetVector.fromBitSet(bs, 125);
  }

  @Test
  void cardinality() {
    assertEquals(6, shortBitVector.getCardinality());
  }

  @Test
  void length() {
    assertEquals(125, shortBitVector.getLength());
  }

  @Test
  void serialize() {
    byte[] s = SerializationUtils.serialize(shortBitVector);
    BitVector bvClone = SerializationUtils.deserialize(s);
    assertEquals(shortBitVector, bvClone);
  }
}
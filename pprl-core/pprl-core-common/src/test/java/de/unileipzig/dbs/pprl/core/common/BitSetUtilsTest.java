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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BitSetUtilsTest {

  private final List<BitSet> bitSets = new ArrayList<>();

  @BeforeEach
  void setUp() {
    BitSet bs = new BitSet(10);
    bs.set(2);
    bs.set(7);
    bitSets.add(bs);
    bs = new BitSet(20);
    bs.set(0);
    bs.set(3);
    bs.set(19);
    bitSets.add(bs);

    bs = new BitSet(5);
    bitSets.add(bs);

    bitSets.add(BitSetUtils.generateRandomBitSet(128, 5));
    bitSets.add(BitSetUtils.generateRandomBitSet(1024, 60));
    bitSets.add(BitSetUtils.generateRandomBitSet(2048, 200));
    bitSets.add(BitSetUtils.generateRandomBitSet(4096, 300));
  }

  @Test
  void bitString() {
    bitSets.forEach(bitSet -> {
      String bitString = BitSetUtils.toBitString(bitSet);
      BitSet bitSetRecovered = BitSetUtils.fromBitString(bitString);
      assertEquals(bitSet, bitSetRecovered);
    });
  }

  @Test
  void base64() {
    bitSets.forEach(bitSet -> {
      String base64 = BitSetUtils.toBase64(bitSet);
      BitSet bitSetRecovered = BitSetUtils.fromBase64(base64);
      assertEquals(bitSet, bitSetRecovered);
    });
  }

  @Test
  void invert() {
    int len = 10;
    int card = 3;
    BitSet bs = BitSetUtils.generateRandomBitSet(len, card);
    BitSet inv = BitSetUtils.invert(bs, len);

    assertEquals(len - card, inv.cardinality());
    assertEquals(len, BitSetUtils.xor(bs, inv).cardinality());
  }

  @Test
  void generateRandomBitSet() {
    BitSet bs = BitSetUtils.generateRandomBitSet(1024, 60);
    assertEquals(60, bs.cardinality());
  }

}
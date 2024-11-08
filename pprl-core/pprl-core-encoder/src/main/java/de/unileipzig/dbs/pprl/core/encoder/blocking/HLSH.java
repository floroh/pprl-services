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

import de.unileipzig.dbs.pprl.core.common.factories.BlockingKeyFactory;
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.BitVector;
import de.unileipzig.dbs.pprl.core.common.model.api.BlockingKey;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * HLSH based blocking on bit vector attributes
 */
public class HLSH extends SingleAttributeBlocker {
  public static final int LSH_KEYS_DEFAULT = 3;
  public static final int LSH_HASHES_DEFAULT = 10;
  public static final int SEED_DEFAULT = 123456;

  private long seed = SEED_DEFAULT;

  /**
   * Number of keys per field
   */
  private int lshKeys = LSH_KEYS_DEFAULT;

  /**
   * Number of hash functions per field
   */
  private int lshHashes = LSH_HASHES_DEFAULT;

  private int bfSize;

  private List<BitSet> bitSetMasks;

  public HLSH(String id, String attributeKey, int bfSize, long seed, int lshKeys, int lshHashes) {
    super(id, attributeKey);
    this.bfSize = bfSize;
    this.seed = seed;
    this.lshKeys = lshKeys;
    this.lshHashes = lshHashes;
//		initMasks();
  }

  public HLSH(String id, String attributeKey, int bfSize, long seed) {
    super(id, attributeKey);
    this.bfSize = bfSize;
    this.seed = seed;
//		initMasks();
  }

  private HLSH() {
  }

  @Override
  public Collection<BlockingKey> extract(Attribute attribute) {
    if (bitSetMasks == null) {
      initMasks();
    }
    Set<BlockingKey> blockingKeys = new HashSet<>();
    if (!attribute.isType(BitVector.class)) {
      throw new RuntimeException("Failed to extract blockingkeys from non-bitvector attribute");
    }
    BitVector attributeValue = (BitVector) attribute.getObject();
    blockingKeys.addAll(getLshKeys(attributeValue.getBitSet()));
    return blockingKeys;
  }

  private Set<BlockingKey> getLshKeys(BitSet bs) {
    final Set<BlockingKey> blockingKeys = new HashSet<>();
    for (int keyIdx = 0; keyIdx < bitSetMasks.size(); keyIdx++) {
      final BitSet bitSetMask = bitSetMasks.get(keyIdx);
      final StringBuilder bkValue = new StringBuilder();
      bitSetMask.stream()
        .forEach(i -> {
          final boolean bitValue = bs.get(i);
          final char value = bitValue ? '1' : '0';
          bkValue.append(value);
        });
      String val = bkValue.toString();
      // Do not allow a BlockingKey that consists of zeros only
//            if (!val.contains("1")) continue;
      blockingKeys.add(BlockingKeyFactory.getBlockingKey(id + "_" + keyIdx, val));
    }
    return blockingKeys;
  }

  private void initMasks() {
    bitSetMasks = new ArrayList<>();
    for (int keyIdx = 0; keyIdx < this.lshKeys; keyIdx++) {
      final BitSet bs = new BitSet(bfSize);
      final Random rnd = new Random(this.seed * keyIdx);
      fillBitSet(bs, rnd);
      bitSetMasks.add(bs);
    }
  }

  /**
   * Set the bit positions in the bitset mask for a specific field / bitset.
   *
   * @param bs  The bitset mask to fill
   * @param rnd The pseudorandom generator
   */
  private void fillBitSet(BitSet bs, Random rnd) {
    for (int hashIdx = 0; hashIdx < this.lshHashes; hashIdx++) {
      int bitIdx;
      do {
        bitIdx = getNextAllowedBitPosition(rnd);
      } while (bs.get(bitIdx));
      bs.set(bitIdx);
    }
  }

  /**
   * Get a new random bit position for a specific field / bitset.
   *
   * @param rnd The pseudorandom generator
   * @return The selected bit position
   */
  private int getNextAllowedBitPosition(Random rnd) {
    final int chosenBitSetSize = this.bfSize;
    int bitIdx;
    int count = 0;
    do {
      bitIdx = rnd.nextInt(chosenBitSetSize);
      if (count > 100) {
        break;
      }
      count++;
    } while (this.isFrequentBitPosition(bitIdx));
    return bitIdx;
  }

  /**
   * Check whether a bit position of a field is marked as frequent
   *
   * @param position The bit position to check
   */
  private boolean isFrequentBitPosition(int position) {
    return false;
  }

  public long getSeed() {
    return seed;
  }

  public int getLshKeys() {
    return lshKeys;
  }

  public int getLshHashes() {
    return lshHashes;
  }

  public int getBfSize() {
    return bfSize;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    HLSH hlsh = (HLSH) o;

    return new EqualsBuilder().append(seed, hlsh.seed)
      .append(lshKeys, hlsh.lshKeys)
      .append(lshHashes, hlsh.lshHashes)
      .append(bfSize, hlsh.bfSize)
      .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37).append(seed)
      .append(lshKeys)
      .append(lshHashes)
      .append(bfSize)
      .toHashCode();
  }
}


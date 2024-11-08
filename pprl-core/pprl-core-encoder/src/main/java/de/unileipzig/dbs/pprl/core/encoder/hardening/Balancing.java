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

import de.unileipzig.dbs.pprl.core.common.model.impl.BitSetVector;
import de.unileipzig.dbs.pprl.core.common.model.api.BitVector;

import java.util.BitSet;
import java.util.Random;

/**
 * Harden a {@link BitVector} by concatenating the inverted bit set
 * and permuting the result.
 * The resulting BitVector is therefore guaranteed to have an equal share of
 * '0' and '1' bits, which prevents frequency attacks based on the cardinality.
 */
public class Balancing implements BitVectorHardener {

  /**
   * Seed for the permutation order
   */
  private long seed;

  public Balancing(long seed) {
    this.seed = seed;
  }

  private Balancing() {
  }

  @Override
  public BitVector harden(BitVector original) {
    int len = original.getLength();
    BitSet bsOrig = original.getBitSet();
    BitVector result = new BitSetVector(2 * len);
    for (int i = 0; i < original.getLength(); i++) {
      result.set(i, bsOrig.get(i));
      result.set(i + len, !bsOrig.get(i));
    }
    permute(result);
    return result;
  }

  public long getSeed() {
    return seed;
  }

  /**
   * Permute the bitvector using the Fisher–Yates shuffle algorithm
   *
   * @param bv bitvector to permute
   */
  private void permute(BitVector bv) {
    Random r = new Random(seed);
    for (int i = bv.getLength() - 1; i > 0; i--) {
      int index = r.nextInt(i);
      BitSet bs = bv.getBitSet();
      boolean tmp = bs.get(index);
      bv.set(index, bs.get(i));
      bv.set(i, tmp);
    }
  }

  @Override
  public String toString() {
    return "Balancing{" + "seed=" + seed + '}';
  }
}

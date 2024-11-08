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

/**
 * Harden a {@link BitVector} by combining its halves using
 * the bitwise XOR operation
 */
public class XorFolding implements BitVectorHardener {
  //TODO Add possibility to fold multiple times
  @Override
  public BitVector harden(BitVector original) {
    int len = original.getLength();
    assert (len % 2 == 0);

    BitSet bsOrig = original.getBitSet();
    BitSet bsLeft = bsOrig.get(0, len / 2);
    BitSet bsRight = bsOrig.get(len / 2 + 1, len);
    bsLeft.xor(bsRight);

    return new BitSetVector(len / 2, bsLeft);
  }

  @Override
  public String toString() {
    return "XorFolding";
  }
}

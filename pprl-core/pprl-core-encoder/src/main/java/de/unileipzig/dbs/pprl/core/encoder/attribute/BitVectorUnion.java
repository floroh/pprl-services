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

package de.unileipzig.dbs.pprl.core.encoder.attribute;

import de.unileipzig.dbs.pprl.core.common.model.impl.BitSetVector;
import de.unileipzig.dbs.pprl.core.common.model.api.BitVector;

import java.util.Map;

/**
 * Merge multiple {@link BitVector}s into one using the union operation
 */
public class BitVectorUnion implements AttributeMerger<BitVector> {

  @Override
  public BitVector merge(Map<String, BitVector> attributes) {
    BitVector bv = null;
    for (BitVector curBV : attributes.values()) {
      if (bv == null) {
        bv = new BitSetVector(curBV.getLength());
      }
      bv.or(curBV);
    }
    return bv;
  }

  @Override
  public String toString() {
    return "BitVectorUnion";
  }
}

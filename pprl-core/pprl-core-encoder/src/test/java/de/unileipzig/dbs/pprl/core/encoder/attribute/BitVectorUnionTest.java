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
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BitVectorUnionTest {

  @Test
  void merge() {
    Map<String, BitVector> attributes = Stream.of("00100010", "10000010")
      .map(BitSetVector::fromBitString)
      .collect(Collectors.toMap(bv -> UUID.randomUUID()
        .toString(), attr -> attr));

    AttributeMerger<BitVector> clk = new BitVectorUnion();
    BitVector result = clk.merge(attributes);

    assertEquals(BitSetVector.fromBitString("10100010")
      .getBitString(), result.getBitString());
  }
}
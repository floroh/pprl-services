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

package de.unileipzig.dbs.pprl.core.common.model.api;

import java.io.Serializable;
import java.util.BitSet;

/**
 * Class representing a bitvector which is an array of bits that can be used to check set membership
 * and to store encoded information
 */
public interface BitVector extends Serializable {

  /**
   * Get the vector as a {@link java.util.BitSet}
   *
   * @return BitSet representation
   */
  BitSet getBitSet();

  /**
   * Get the vector as a String using Base64 for compression
   *
   * @return String representation
   */
  String getBase64();

  /**
   * Get the vector as String consisting of '0' and '1' characters
   *
   * @return String representation
   */
  String getBitString();

  /**
   * Set a position in the vector
   *
   * @param pos   position to be set
   * @param value value to set
   */
  void set(int pos, boolean value);

  /**
   * Set a position in the vector to 1
   *
   * @param pos position to be set
   */
  default void set(int pos) {
    set(pos, true);
  }

  /**
   * Length of the bitvector
   *
   * @return length in bits
   */
  int getLength();

  /**
   * Get cardinality (number of bit positions with value 1)
   *
   * @return int representation of the cardinality
   */
  int getCardinality();

  /**
   * Apply bitwise AND operation with a second bitvector
   *
   * @param bvOther second bitvector
   */
  void and(BitVector bvOther);

  /**
   * Apply bitwise OR operation with a second bitvector
   *
   * @param bvOther second bitvector
   */
  void or(BitVector bvOther);

  /**
   * Get byte representation of the bitvector
   *
   * @return byte representation of this bitvector
   */
  byte[] toByteArray();

}

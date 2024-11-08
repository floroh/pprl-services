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

package de.unileipzig.dbs.pprl.core.common.model.impl;

import de.unileipzig.dbs.pprl.core.common.BitSetUtils;
import de.unileipzig.dbs.pprl.core.common.ByteUtils;
import de.unileipzig.dbs.pprl.core.common.model.api.BitVector;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Objects;

/**
 * Default implementation of the {@link BitVector} interface
 * that stores the bit set using the java.util.BitSet class.
 */
public class BitSetVector implements BitVector {
  private int length;
  private BitSet bitSet;

  public BitSetVector() {
    bitSet = new BitSet();
  }

  public BitSetVector(int length) {
    this.length = length;
    bitSet = new BitSet(length);
  }

  public BitSetVector(int length, BitSet bitSet) {
    this(bitSet);
    this.length = length;
  }

  private BitSetVector(BitSet bitSet) {
    if (bitSet == null) {
      throw new RuntimeException("Empty BitSet used for BitSetVector construction");
    }
    this.length = bitSet.size();
    this.bitSet = bitSet;
  }

  @Override
  public void set(int pos, boolean value) {
    bitSet.set(pos, value);
  }

  @Override
  public int getLength() {
    return length;
  }

  @Override
  public int getCardinality() {
    return bitSet.cardinality();
  }

  @Override
  public void and(BitVector bvOther) {
    this.bitSet = BitSetUtils.and(this.bitSet, bvOther.getBitSet());
    this.length = Math.max(this.length, bvOther.getLength());
  }

  @Override
  public void or(BitVector bvOther) {
    this.bitSet = BitSetUtils.or(this.bitSet, bvOther.getBitSet());
    this.length = Math.max(this.length, bvOther.getLength());
  }

  @Override
  public BitSet getBitSet() {
    return this.bitSet;
  }

  @Override
  public String getBase64() {
    return BitSetUtils.toBase64(bitSet);
  }

  @Override
  public String getBitString() {
    return BitSetUtils.toBitString(bitSet, length);
  }

  @Override
  public byte[] toByteArray() {
    byte[] valueBytes = bitSet.toByteArray();
    byte[] lengthBytes = ByteUtils.intToByteArray(length);
    byte[] bytes = new byte[valueBytes.length + lengthBytes.length];
    System.arraycopy(lengthBytes, 0, bytes, 0, lengthBytes.length);
    System.arraycopy(valueBytes, 0, bytes, lengthBytes.length, valueBytes.length);
    return bytes;
  }

  public static BitSetVector fromByteArray(byte[] bytes) {
    byte[] subArray = Arrays.copyOfRange(bytes, 0, Integer.BYTES);
    int length = ByteUtils.intFromByteArray(subArray);
    subArray = Arrays.copyOfRange(bytes, Integer.BYTES, bytes.length);
    BitSet bs = BitSet.valueOf(subArray);
    return new BitSetVector(length, bs);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BitSetVector that = (BitSetVector) o;
    return length == that.length && Objects.equals(bitSet, that.bitSet);
  }

  @Override
  public int hashCode() {
    return Objects.hash(length, bitSet);
  }

  @Override
  public String toString() {
    return "BitSetVector{" + "length=" + length + ", cardinality=" + getCardinality() + '}';
  }

  /**
   * Create an instance from BitSetVector from a {@link java.util.BitSet}
   *
   * @param bs BitSet
   * @param length length of the bitvector
   * @return BitSetVector object
   */
  public static BitSetVector fromBitSet(BitSet bs, int length) {
    return new BitSetVector(length, bs);
  }

  /**
   * Create an instance from BitSetVector from a String, that was generated using
   * {@link BitVector#getBase64()}
   *
   * @param b64 String containing a bitvector that was compressed using Base64
   * @return BitSetVector object
   */
  public static BitSetVector fromBase64(String b64) {
    return new BitSetVector(BitSetUtils.fromBase64(b64));
  }

  /**
   * Create an instance from BitSetVector from a String of '0' and '1' characters (see
   * {@link BitVector#getBitString()}
   *
   * @param str String containing a bitvector
   * @return BitSetVector object
   */
  public static BitSetVector fromBitString(String str) {
    return new BitSetVector(str.length(), BitSetUtils.fromBitString(str));
  }
}

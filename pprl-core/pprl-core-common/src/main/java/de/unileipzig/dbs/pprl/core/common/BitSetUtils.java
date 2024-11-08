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

import org.apache.commons.lang3.ArrayUtils;

import java.util.Base64;
import java.util.BitSet;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * Utils for generating, converting and operating on {@link BitSet} objects
 */
public final class BitSetUtils {
  private static final boolean USE_LITTLE_ENDIAN = true;

  private static Base64.Decoder b64Decoder = Base64.getDecoder();
  private static Base64.Encoder b64Encoder = Base64.getEncoder();

  private BitSetUtils() {
    throw new RuntimeException();
  }

  public static BitSet fromBitString(String s) {
    final BitSet bitset = new BitSet(s.length());
    final int lastBitIndex = s.length() - 1;

    for (int i = lastBitIndex; i >= 0; i--) {
      if (s.charAt(i) == '1') {
        bitset.set(USE_LITTLE_ENDIAN ? i : lastBitIndex - i);
      }
    }
    return bitset;
  }

  public static String toBitString(BitSet bs, int length) {
    final StringBuilder buffer = new StringBuilder(length);
    IntStream.range(0, length)
      .mapToObj(i -> bs.get(USE_LITTLE_ENDIAN ? i : length - i - 1) ? '1' : '0')
      .forEach(buffer::append);
    return buffer.toString();
  }

  public static String toBitString(BitSet bs) {
    return toBitString(bs, bs.length());
  }

  public static BitSet fromBase64(String s) {
    byte[] bytes = b64Decoder.decode(s);
    if (!USE_LITTLE_ENDIAN) {
      ArrayUtils.reverse(bytes);
    }
    return BitSet.valueOf(bytes);
  }

  public static String toBase64(BitSet bs) {
    byte[] bytes = bs.toByteArray();
    if (!USE_LITTLE_ENDIAN) {
      ArrayUtils.reverse(bytes);
    }
    return b64Encoder.encodeToString(bytes);
  }

  public static BitSet and(BitSet first, BitSet second) {
    if (first != null && second != null) {
      BitSet bitset = (BitSet) first.clone();
      bitset.and(second);
      return bitset;
    } else {
      return null;
    }
  }

  public static BitSet or(BitSet first, BitSet second) {
    if (first != null && second != null) {
      BitSet bitset = (BitSet) first.clone();
      bitset.or(second);
      return bitset;
    } else {
      return null;
    }
  }

  public static BitSet xor(BitSet first, BitSet second) {
    if (first != null && second != null) {
      BitSet bitset = (BitSet) first.clone();
      bitset.xor(second);
      return bitset;
    } else {
      return null;
    }
  }

  public static BitSet invert(BitSet bs, int len) {
    BitSet inv = (BitSet) bs.clone();
    inv.xor(filled(len));
    return inv;
  }

  public static BitSet filled(int len) {
    BitSet bs = new BitSet(len);
    bs.set(0, len);
    return bs;
  }

  public static BitSet generateRandomBitSet(int length, int numSetBits) {
    BitSet bs = new BitSet(length);
    Random rand = new Random();
    while (bs.cardinality() < numSetBits) {
      bs.set(rand.nextInt(length));
    }
    return bs;
  }
}

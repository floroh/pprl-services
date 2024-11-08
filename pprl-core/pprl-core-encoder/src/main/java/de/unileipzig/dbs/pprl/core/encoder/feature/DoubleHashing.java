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

package de.unileipzig.dbs.pprl.core.encoder.feature;

import de.unileipzig.dbs.pprl.core.common.HashUtils;
import de.unileipzig.dbs.pprl.core.common.model.impl.BitSetVector;
import de.unileipzig.dbs.pprl.core.common.model.api.BitVector;

/**
 * Convert a String into {@link BitVector} by hashing it multiple times
 * using the Double Hashing scheme.
 * In contrast to {@link RandomHashing} no salting is supported by this method.
 */
public class DoubleHashing implements FeatureEncoder<String, BitVector> {
  /**
   * Number of hash functions to apply on each feature which is (ignoring collusion) the number of bits
   * to be set to 1 in the {@link BitVector}
   */
  private int numHashFunctions;

  private String algorithm0;

  private String algorithm1;

  public DoubleHashing(int numHashFunctions) {
    this(numHashFunctions, "MD5", "SHA-256");
  }

  public DoubleHashing(int numHashFunctions, String algorithm0, String algorithm1) {
    this.numHashFunctions = numHashFunctions;
    this.algorithm0 = algorithm0;
    this.algorithm1 = algorithm1;
  }

  private DoubleHashing() {
  }

  @Override
  public BitVector encode(int bvLength, String feature) {
    final BitVector bv = new BitSetVector(bvLength);
    for (int i = 0; i < numHashFunctions; i++) {
      final int position = hash(feature, i, bvLength, algorithm0, algorithm1);
      bv.set(position);
    }
    return bv;
  }

  public static int hash(String element, int hashNumber, int bvLength, String algorithm0, String algorithm1) {
    int hash0 = hashElement(element, algorithm0);
    int hash1 = hashElement(element, algorithm1);
    return (Math.abs(hash0 + hashNumber * hash1))  % bvLength;
  }

  private static int hashElement(String element, String algorithm) {
    return Math.abs(HashUtils.getHash(algorithm, element));
  }

  public int getNumHashFunctions() {
    return numHashFunctions;
  }

  @Override
  public String toString() {
    return "DoubleHashing{" + "numHashFunctions=" + numHashFunctions + '}';
  }
}

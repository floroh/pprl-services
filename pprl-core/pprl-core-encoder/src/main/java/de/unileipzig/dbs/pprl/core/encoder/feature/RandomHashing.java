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

import java.util.Random;

/**
 * Convert a String to a {@link BitVector} by hashing it multiple times
 * using the Random Hashing scheme.
 * Salting is used to create different encodings for the same String.
 */
public class RandomHashing implements FeatureEncoder<String, BitVector> {

  /**
   * Number of hash functions to apply on each feature which is (ignoring collusion) the number of bits
   * to be set to 1 in the {@link BitVector}
   */
  private int numHashFunctions;
  private String salt;

  public RandomHashing(int numHashFunctions, String salt) {
    this.numHashFunctions = numHashFunctions;
    this.salt = salt;
  }

  private RandomHashing() {
  }

  @Override
  public BitVector encode(int bvLength, String feature) {
    final BitVector bv = new BitSetVector(bvLength);
    final Random r = HashUtils.getRandom(feature, salt);

    for (int i = 0; i < numHashFunctions; i++) {
      bv.set(r.nextInt(bvLength));
    }
    return bv;
  }

  public int getNumHashFunctions() {
    return numHashFunctions;
  }

  public void setNumHashFunctions(int numHashFunctions) {
    this.numHashFunctions = numHashFunctions;
  }

  public String getSalt() {
    return salt;
  }

  public void setSalt(String salt) {
    this.salt = salt;
  }

}

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

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.unileipzig.dbs.pprl.core.common.ByteUtils;
import de.unileipzig.dbs.pprl.core.common.HashUtils;
import de.unileipzig.dbs.pprl.core.common.model.api.BitVector;
import de.unileipzig.dbs.pprl.core.common.model.impl.BitSetVector;
import de.unileipzig.dbs.pprl.core.encoder.KeyManager;
import de.unileipzig.dbs.pprl.core.encoder.crypto.KeyedEncoderComponent;

import java.security.Key;
import java.util.Random;

/**
 * Convert a String to a {@link BitVector} by hashing it multiple times
 * using the Random Hashing scheme.
 * Salting is used to create different encodings for the same String.
 */
public class KeyStoreRandomHashing implements FeatureEncoder<String, BitVector>, KeyedEncoderComponent {

  /**
   * Number of hash functions to apply on each feature which is (ignoring collusion) the number of bits
   * to be set to 1 in the {@link BitVector}
   */
  private int numHashFunctions;

  @JsonIgnore
  private byte[] secret = null;

  private String keyName;

  @JsonIgnore
  private byte[] additionalSecret = new byte[0];

  public KeyStoreRandomHashing(int numHashFunctions, String keyName) {
    this.numHashFunctions = numHashFunctions;
    this.keyName = keyName;
  }

  private KeyStoreRandomHashing() {
  }

  @Override
  public BitVector encode(int bvLength, String feature) {
    if (secret == null) {
      initSecret();
    }
    byte[] currentSecret = ByteUtils.concatByteArrays(secret, additionalSecret);
    final BitVector bv = new BitSetVector(bvLength);
    final Random r = HashUtils.getRandom(feature, currentSecret);
    for (int i = 0; i < numHashFunctions; i++) {
      bv.set(r.nextInt(bvLength));
    }
    return bv;
  }

  @Override
  public void setKey(String key) {
    if (key == null) {
      this.additionalSecret = new byte[0];
      return;
    }
    additionalSecret = key.getBytes();
  }

  private void initSecret() {
    Key key = KeyManager.getDerivedSecret(keyName);
    secret = key.getEncoded();
  }

  public int getNumHashFunctions() {
    return numHashFunctions;
  }

  public String getKeyName() {
    return keyName;
  }

  public void setKeyName(String keyName) {
    this.keyName = keyName;
  }

}

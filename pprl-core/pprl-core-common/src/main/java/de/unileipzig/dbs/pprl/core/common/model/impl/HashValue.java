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

import java.util.Arrays;

public class HashValue {

  private byte[] hash;

  public HashValue(byte[] hash) {
    this.hash = hash;
  }

  public byte[] getHash() {
    return hash;
  }

  public void setHash(byte[] hash) {
    this.hash = hash;
  }

  public HashValue xor(HashValue other) {
    byte[] hash = Arrays.copyOf(this.hash, this.hash.length);
    byte[] otherHash = other.getHash();
    int length = Math.min(hash.length, otherHash.length);
    for (int i = 0; i < length; i++) {
      hash[i] = xor(hash[i], otherHash[i]);
    }
    return new HashValue(hash);
  }

  private byte xor(byte b0, byte b1) {
    return (byte) (0xff & ((int) b0) ^ ((int) b1));
  }

  @Override
  public String toString() {
    return "HashValue{" + "hash=" + Arrays.toString(hash) + '}';
  }
}

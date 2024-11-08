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

import de.unileipzig.dbs.pprl.core.common.HashUtils;

import java.nio.charset.StandardCharsets;

public class HashEncoder implements AttributeEncoder<String, String> {

  private String id;

  private String salt;

  public HashEncoder(String id, String salt) {
    this.id = id;
    this.salt = salt;
  }

  private HashEncoder() {
  }

  @Override
  public String encode(String attribute) {
//        return (HashUtils.getHash(attribute, salt));
    return new String(HashUtils.getHMacBytes(attribute, salt), StandardCharsets.UTF_8);
  }

  @Override
  public Class<String> getInputClass() {
    return String.class;
  }

  @Override
  public Class<String> getOutputClass() {
    return String.class;
  }

  @Override
  public String getId() {
    return id;
  }

  public String getSalt() {
    return salt;
  }

  public void setSalt(String salt) {
    this.salt = salt;
  }
}

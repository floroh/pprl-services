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

package de.unileipzig.dbs.pprl.core.common.model;

import de.unileipzig.dbs.pprl.core.common.BitSetUtils;
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.BitVector;
import de.unileipzig.dbs.pprl.core.common.model.impl.AttributeLight;
import de.unileipzig.dbs.pprl.core.common.model.impl.BitSetVector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AttributeLightTest {
  private static final String TEST_STRING = "TestString";
  private static final int TEST_INTEGER = 123;
  private static final BitVector TEST_BITVECTOR = BitSetVector.fromBitSet(
    BitSetUtils.generateRandomBitSet(1024, 300), 1024);


  @Test
  void typeString() {
    Attribute attr = new AttributeLight(TEST_STRING);
    assertTrue(attr.isType(String.class));
    assertFalse(attr.isType(Integer.class));
    assertEquals(TEST_STRING, attr.getObject());

    String strRepresentation = attr.toString();
    assertNotNull(strRepresentation);
    assertFalse(strRepresentation.isEmpty());
  }

  @Test
  void typeInteger() {
    Attribute attr = new AttributeLight(TEST_INTEGER);
    assertTrue(attr.isType(Integer.class));
    assertEquals(TEST_INTEGER, (int) attr.getObject());

    String strRepresentation = attr.toString();
    assertNotNull(strRepresentation);
    assertFalse(strRepresentation.isEmpty());
  }

  @Test
  void typeBitVector() {
    Attribute attr = new AttributeLight(TEST_BITVECTOR);
    assertTrue(attr.isType(BitVector.class));
    assertEquals(TEST_BITVECTOR, attr.getObject());

    String strRepresentation = attr.toString();
    assertNotNull(strRepresentation);
    assertFalse(strRepresentation.isEmpty());
  }

  @Test
  void typeNull() {
    Attribute attr = new AttributeLight(null);
    assertTrue(attr.isNull());
    assertNull(attr.getObject());

    String strRepresentation = attr.toString();
    assertNotNull(strRepresentation);
    assertFalse(strRepresentation.isEmpty());
  }
}
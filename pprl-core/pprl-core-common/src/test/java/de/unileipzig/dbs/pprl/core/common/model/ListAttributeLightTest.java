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
import de.unileipzig.dbs.pprl.core.common.model.api.BitVector;
import de.unileipzig.dbs.pprl.core.common.model.api.ListAttribute;
import de.unileipzig.dbs.pprl.core.common.model.impl.BitSetVector;
import de.unileipzig.dbs.pprl.core.common.model.impl.ListAttributeLight;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ListAttributeLightTest {
  private static final BitVector TEST_BITVECTOR = BitSetVector.fromBitSet(
    BitSetUtils.generateRandomBitSet(1024, 300), 1024);
  private static final BitVector TEST_BITVECTOR_2 = BitSetVector.fromBitSet(
    BitSetUtils.generateRandomBitSet(512, 100), 512);

  @Test
  void typeBitVector() {
    ListAttribute attr = new ListAttributeLight(Arrays.asList(TEST_BITVECTOR, TEST_BITVECTOR_2));
    assertTrue(attr.isType(BitVector.class));
    assertEquals(TEST_BITVECTOR, ((List) attr.getObject()).get(0));
    assertEquals(TEST_BITVECTOR_2, ((List) attr.getObject()).get(1));

    assertThrows(RuntimeException.class, () -> attr.getListAs(Integer.class));

    List<BitVector> bitVectors = attr.getListAs(BitVector.class);
    assertEquals(2, bitVectors.size());
    assertEquals(TEST_BITVECTOR, bitVectors.get(0));
    assertEquals(TEST_BITVECTOR_2, bitVectors.get(1));

    BitVector bitVector = attr.getAs(BitVector.class);
    assertEquals(TEST_BITVECTOR, bitVector);

    String strRepresentation = attr.toString();
    assertNotNull(strRepresentation);
    assertFalse(strRepresentation.isEmpty());
  }

  @Test
  void typeNullWithEmptyList() {
    ListAttribute attr = new ListAttributeLight(new ArrayList<String>());
    assertTrue(attr.isNull());
    Object obj = attr.getObject();
    assertTrue(obj instanceof List);
    List<String> strList = (List<String>) attr.getObject();
    assertDoesNotThrow(() -> strList.add("Test"));
    List<Integer> intList = (List<Integer>) attr.getObject();
    assertDoesNotThrow(() -> intList.add(5));

    String strRepresentation = attr.toString();
    assertNotNull(strRepresentation);
    assertFalse(strRepresentation.isEmpty());
  }

  @Test
  void typeNull() {
    ListAttribute attr = new ListAttributeLight(null);
    assertTrue(attr.isNull());
    assertNull(attr.getObject());

    String strRepresentation = attr.toString();
    assertNotNull(strRepresentation);
    assertFalse(strRepresentation.isEmpty());
  }
}
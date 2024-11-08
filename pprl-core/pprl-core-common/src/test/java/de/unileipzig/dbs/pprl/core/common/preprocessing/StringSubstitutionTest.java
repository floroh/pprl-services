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

package de.unileipzig.dbs.pprl.core.common.preprocessing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StringSubstitutionTest {

  @Test
  void umlauts() {
    StringSubstitution n = new StringSubstitution();
    assertEquals("Baecker Mueller Oehler", n.preprocess("Bäcker Müller Öhler"));
  }

  @Test
  void esszett() {
    StringSubstitution n = new StringSubstitution();
    assertEquals("Assmann", n.preprocess("Aßmann"));
  }
}
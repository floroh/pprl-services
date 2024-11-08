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

import de.unileipzig.dbs.pprl.core.common.CommonTestBase;
import de.unileipzig.dbs.pprl.core.common.factories.AttributeFactory;
import de.unileipzig.dbs.pprl.core.common.factories.RecordFactory;
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.impl.PersonalAttributeType;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DateSplitterTest extends CommonTestBase {

  @Test
  void preprocessDefault() {
    Record in = getPersonalRecord();
    assertTrue(in.getAttribute(PersonalAttributeType.DATEOFBIRTH.name())
      .isPresent());
    DateSplitter splitter = new DateSplitter();
    Record out = splitter.preprocess(in);
    assertTrue(out.getAttribute(PersonalAttributeType.DATEOFBIRTH.name())
      .isEmpty());
    assertTrue(out.getAttribute(PersonalAttributeType.DAYOFBIRTH.name())
      .isPresent());
    assertTrue(out.getAttribute(PersonalAttributeType.MONTHOFBIRTH.name())
      .isPresent());
    assertTrue(out.getAttribute(PersonalAttributeType.YEAROFBIRTH.name())
      .isPresent());
  }

  @Test
  void preprocessCustom() {
    String dobName = "customDobName";
    Record in = RecordFactory.getEmptyRecord();
    in.setAttribute(dobName, AttributeFactory.getAttribute("1956-08-14"));

    DateSplitter splitter = new DateSplitter();
    splitter.setInputDatePattern("yyyy-MM-dd");
    splitter.setKeepFullDate(true);
    splitter.setAttributeId(dobName);

    Record out = splitter.preprocess(in);

    assertTrue(out.getAttribute(dobName).isPresent());
    Optional<Attribute> dayAttribute = out.getAttribute(PersonalAttributeType.DAYOFBIRTH.name());
    Optional<Attribute> monthAttribute = out.getAttribute(PersonalAttributeType.MONTHOFBIRTH.name());
    Optional<Attribute> yearAttribute = out.getAttribute(PersonalAttributeType.YEAROFBIRTH.name());
    assertTrue(dayAttribute.isPresent());
    assertEquals("14", dayAttribute.get().getAsString());
    assertTrue(monthAttribute.isPresent());
    assertEquals("08", monthAttribute.get().getAsString());
    assertTrue(yearAttribute.isPresent());
    assertEquals("1956", yearAttribute.get().getAsString());
  }
}
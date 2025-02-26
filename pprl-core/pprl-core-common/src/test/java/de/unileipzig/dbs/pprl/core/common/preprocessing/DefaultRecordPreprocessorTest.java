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
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.ListAttribute;
import de.unileipzig.dbs.pprl.core.common.model.impl.PersonalAttributeType;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultRecordPreprocessorTest extends CommonTestBase {

  @Test
  void firstName() {
    DefaultRecordPreprocessor preprocessor =
      new DefaultRecordPreprocessor(
        PersonalAttributeType.FIRSTNAME.toString(),
        new StringAttributeSplitter()
      );

    Record record = getPersonalRecord(3);
    Record recordWithSplitFirstName = preprocessor.preprocess(record);
    Optional<Attribute> fn =
      recordWithSplitFirstName.getAttribute(PersonalAttributeType.FIRSTNAME.toString());
    assertTrue(fn.isPresent());
    assertTrue(fn.get() instanceof ListAttribute);
    List<String> fns = ((ListAttribute) fn.get()).getListAs(String.class);
    assertEquals(2, fns.size());
  }

  @Test
  void lastName() {
    DefaultRecordPreprocessor preprocessor =
      new DefaultRecordPreprocessor(PersonalAttributeType.LASTNAME.toString(), new StringAttributeSplitter());

    Record record = getPersonalRecord(3);
    Record recordWithSplitLastName = preprocessor.preprocess(record);
    Optional<Attribute> fn = recordWithSplitLastName.getAttribute(PersonalAttributeType.LASTNAME.toString());
    assertTrue(fn.isPresent());
    assertTrue(fn.get() instanceof ListAttribute);
    List<String> fns = ((ListAttribute) fn.get()).getListAs(String.class);
    assertEquals(2, fns.size());
  }

}
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

package de.unileipzig.dbs.pprl.core.common.factories;

import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordId;
import de.unileipzig.dbs.pprl.core.common.model.impl.RecordLight;
import de.unileipzig.dbs.pprl.core.common.model.impl.RecordSimple;

import java.util.Map;
import java.util.UUID;

public class RecordFactory {
  public enum RecordVariant {DEFAULT, LIGHT}

  public static Record getEmptyRecord() {
    return getEmptyRecord(
      RecordIdFactory.get(UUID.randomUUID().toString())
    );
  }

  public static Record getEmptyRecord(RecordId recordId) {
    return getEmptyRecord(RecordVariant.DEFAULT, recordId);
  }

  public static Record getEmptyRecord(RecordVariant variant, RecordId recordId) {
    switch (variant) {
      default:
      case DEFAULT:
        return new RecordSimple(recordId);
      case LIGHT:
        return new RecordLight(recordId);
    }
  }

  public static Record getRecord(RecordVariant variant, RecordId recordId,
    Map<String, Attribute> attributes) {
    switch (variant) {
      default:
      case DEFAULT:
        return new RecordSimple(recordId, attributes);
      case LIGHT:
        return new RecordLight(recordId, attributes);
    }
  }

  public static Record getRecordDuplicate(RecordVariant variant, Record record) {
    Record dup = record.duplicate();
    return getRecord(variant, dup.getId(), dup.getAttributes());
  }
}

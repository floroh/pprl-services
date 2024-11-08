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

package de.unileipzig.dbs.pprl.core.analyzer.attribute;

import de.unileipzig.dbs.pprl.core.analyzer.Analyzer;
import de.unileipzig.dbs.pprl.core.analyzer.results.ResultSet;
import de.unileipzig.dbs.pprl.core.common.RecordUtils;
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Analyzer of all attributes values grouped by the attribute name
 * e.g. average length of the attributes
 */
public abstract class AttributeAnalyzer extends Analyzer {

  protected int recordCount;

  public void setRecordCount(int recordCount) {
    this.recordCount = recordCount;
  }

  public abstract ResultSet analyze(Map<String, List<Attribute>> attributes);

  public static Map<String, List<Attribute>> prepareRecords(Collection<Record> records) {
    return RecordUtils.groupByAttributeName(records);
  }
}

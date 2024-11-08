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

package de.unileipzig.dbs.pprl.core.analyzer.record;

import de.unileipzig.dbs.pprl.core.analyzer.results.Result;
import de.unileipzig.dbs.pprl.core.analyzer.results.ResultSet;
import de.unileipzig.dbs.pprl.core.common.RecordUtils;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordId;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Get the number of records in the dataset and the groups
 */
public class RecordCounter extends RecordAnalyzer {
  public static final String SOURCE = "source";
  public static final String TOTAL_COUNT = "total";
  public static final String COUNT = "count";

  @Override
  public ResultSet analyze(List<Record> records) {
    ResultSet resultSet = getResultSet();

    Result result;
    BigDecimal total = BigDecimal.ZERO;
    Map<String, List<Record>> groupedRecords = RecordUtils.groupById(records, RecordId.SOURCE_ID);
    for (Map.Entry<String, List<Record>> group : groupedRecords.entrySet()) {
      BigDecimal groupSize = BigDecimal.valueOf(group.getValue()
        .size());
      result = new Result();
      result.setParam(SOURCE, group.getKey());
      result.addMetric(COUNT, groupSize);
      resultSet.addResult(result);
      total = total.add(groupSize);
    }
    result = new Result();
    result.setParam(SOURCE, TOTAL_COUNT);
    result.addMetric(COUNT, total);
    resultSet.addResult(result);

    return resultSet;
  }
}

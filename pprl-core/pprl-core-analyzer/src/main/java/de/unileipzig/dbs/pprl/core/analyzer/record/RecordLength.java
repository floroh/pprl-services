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

import de.unileipzig.dbs.pprl.core.analyzer.results.ResultSet;
import de.unileipzig.dbs.pprl.core.common.RecordUtils;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordId;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static de.unileipzig.dbs.pprl.core.analyzer.attribute.AttributeLength.*;

/**
 * Measures the cumulative length of the records
 * Empty or invalid attribute values are ignored
 */
public class RecordLength extends RecordAnalyzer {
  public static final String SOURCE = "source";
  public static final String TOTAL = "total";

  @Override
  public ResultSet analyze(List<Record> records) {
    ResultSet resultSet = getResultSet();
    resultSet.setDescription("Empty or invalid attribute values (e.g. null, NaN) are ignored. \n" +
            " For Bitvector attributes the cardinality is used as the length");


    Map<String, List<Record>> groupedRecords = RecordUtils.groupById(records, RecordId.SOURCE_ID);
    DescriptiveStatistics totalStats = new DescriptiveStatistics();
    for (Map.Entry<String, List<Record>> group : groupedRecords.entrySet()) {
      DescriptiveStatistics stats = new DescriptiveStatistics();
      for (Record record : group.getValue()) {
        int recordLength = record.getAttributes().entrySet().stream()
                .map(e -> getAttributeLength(e.getKey(), e.getValue()))
                .filter(Objects::nonNull)
                .mapToInt(i -> i)
                .sum();
        stats.addValue(recordLength);
        totalStats.addValue(recordLength);
      }
      resultSet.addResult(buildResult(SOURCE, group.getKey(), stats));
      resultSet.addAdditionalResult(buildLengthDistributionTable(group.getKey(), stats));
    }
    resultSet.addResult(buildResult(SOURCE, TOTAL, totalStats));
    resultSet.addAdditionalResult(buildLengthDistributionTable(TOTAL, totalStats));
    return resultSet;
  }
}

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
import de.unileipzig.dbs.pprl.core.common.HelperUtils;
import de.unileipzig.dbs.pprl.core.common.RecordUtils;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordId;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Get the number of records that are part of multiple groups
 */
public class RecordOverlap extends RecordAnalyzer {
  public static final String SOURCE_PAIR = "source pair";
  public static final String TOTAL_OVERLAP = "total";
  public static final String OVERLAP = "overlap";

  @Override
  public ResultSet analyze(List<Record> records) {
    ResultSet resultSet = getResultSet();

    Map<String, List<Record>> clusters = RecordUtils.groupById(records, RecordId.GLOBAL_ID);
    if (clusters.size() < records.size()) {
      Map<String, Long> counter = new HashMap<>();
      for (List<Record> cluster : clusters.values()) {
        if (cluster.size() == 1) {
          continue;
        }
        List<String> sourceIds = cluster.stream()
          .map(Record::getId)
          .map(RecordId::getSourceId)
          .sorted()
          .collect(Collectors.toList());
        HelperUtils.combination(sourceIds, 2)
          .stream()
          .map(sources -> String.join("-", sources))
          .forEach(s -> {
            if (counter.containsKey(s)) {
              counter.put(s, counter.get(s) + 1);
            } else {
              counter.put(s, 1L);
            }
          });
      }
      BigDecimal total = BigDecimal.ZERO;
      for (Map.Entry<String, Long> c : counter.entrySet()) {
        BigDecimal val = BigDecimal.valueOf(c.getValue());
        Result result = new Result();
        result.setParam(SOURCE_PAIR, c.getKey());
        result.addMetric(OVERLAP, val);
        resultSet.addResult(result);
        total = total.add(val);
      }
      Result result = new Result();
      result.setParam(SOURCE_PAIR, TOTAL_OVERLAP);
      result.addMetric(OVERLAP, total);
      resultSet.addResult(result);
    }
    return resultSet;
  }
}

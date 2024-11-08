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

package de.unileipzig.dbs.pprl.core.analyzer.cluster;

import de.unileipzig.dbs.pprl.core.analyzer.results.Result;
import de.unileipzig.dbs.pprl.core.analyzer.results.ResultSet;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Analyze the cluster of records belonging to the same real world entity
 * by counting how many attributes of these records differ
 */
public class ClusterPairwiseEqualCount extends ClusterPairwiseEqual {
  private static final String FULL_RECORD_NUM_ERRORS = "#errors";
  public static final String COUNT = "count";
  public static final String SHARE = "share";

  @Override
  public ResultSet analyze(Map<String, List<Record>> clusters) {
    ResultSet resultSet = getResultSet();
    resultSet.setDescription(buildDescription());

    Map<String, DescriptiveStatistics> stats = determineAttributewiseEqualityWithinClusters(clusters);
    DescriptiveStatistics fullRecordStats = stats.get(FULL_RECORD);
    long total = fullRecordStats.getN();
    Map<Integer, Long> diffAttributeCounts = Arrays.stream(fullRecordStats.getValues())
      .mapToInt(d -> (int)Math.round(d))
      .boxed()
      .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

    for (Map.Entry<Integer, Long> diffAttributeCount : diffAttributeCounts.entrySet()) {
      String countString = String.format("%d", diffAttributeCount.getKey());
      Result result = new Result();
      result.setParam(FULL_RECORD_NUM_ERRORS, countString);
      result.addMetric(COUNT, BigDecimal.valueOf(diffAttributeCount.getValue()));
      double share = (double) diffAttributeCount.getValue() / total;
      result.addMetric(SHARE, BigDecimal.valueOf(share));
      resultSet.addResult(result);
    }

    return resultSet;
  }

  private String buildDescription() {
    return "All attribute values of the same type are compared on equality within each cluster.\n" +
      "It is counted, how many attributes differ per record pair (independent of the extend of difference).";
  }

  @Override
  public String toString() {
    return "ClusterPairwiseEqualCount";
  }

}

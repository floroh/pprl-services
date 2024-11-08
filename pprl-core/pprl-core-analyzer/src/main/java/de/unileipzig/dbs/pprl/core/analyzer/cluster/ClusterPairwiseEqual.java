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

import de.unileipzig.dbs.pprl.core.analyzer.cluster.data.AttributePair;
import de.unileipzig.dbs.pprl.core.analyzer.cluster.data.Pair;
import de.unileipzig.dbs.pprl.core.analyzer.results.Result;
import de.unileipzig.dbs.pprl.core.analyzer.results.ResultSet;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Analyze the cluster of records belonging to the same real world entity
 * by counting how often the attributes of these true duplicates are equal
 */
public class ClusterPairwiseEqual extends ClusterPairwiseDiff {
  public static final String FULL_RECORD = "full record";

  @Override
  public ResultSet analyze(Map<String, List<Record>> clusters) {
    ResultSet resultSet = getResultSet();
    resultSet.setDescription(buildDescription());

    Map<String, DescriptiveStatistics> stats =
      determineAttributewiseEqualityWithinClusters(clusters);

    List<String> attributeNames = stats.keySet()
      .stream()
      .sorted()
      .collect(Collectors.toList());

    for (String attributeName : attributeNames) {
      Result result = new Result();
      result.setParam(HEADER_ATTRIBUTE, attributeName);
      addDescriptiveStatisticMetrics(result, stats.get(attributeName),
        Arrays.asList("count", "mean", "min", "max")
      );
      resultSet.addResult(result);
    }

    return resultSet;
  }

  protected Map<String, DescriptiveStatistics> determineAttributewiseEqualityWithinClusters(
    Map<String, List<Record>> clusters) {
    Map<String, DescriptiveStatistics> stats = new HashMap<>();
    stats.put(FULL_RECORD, new DescriptiveStatistics());

    for (List<Record> cluster : clusters.values()) {
      List<Pair<Record>> recordPairs = buildRecordPairs(cluster);
      //TODO Normalize on number of pairs to prevent disproportional influence of large clusters?
      for (Pair<Record> recordPair : recordPairs) {
        double recordDistance = 0;

        for (AttributePair ap : buildAttributePairs(recordPair)) {
          if (!stats.containsKey(ap.getAttributeName())) {
            stats.put(ap.getAttributeName(), new DescriptiveStatistics());
          }
          double attrDistance = getEqualityBasedDistance(ap);
          stats.get(ap.getAttributeName())
            .addValue(attrDistance);
          recordDistance += attrDistance;
        }
        stats.get(FULL_RECORD).addValue(recordDistance);
      }
    }
    return stats;
  }

  private String buildDescription() {
    return "All attribute values of the same type are compared on equality within each cluster\n" +
      "The given values are distances (0 = equal, 1 = unequal)";
  }

  @Override
  public String toString() {
    return "ClusterPairwiseEqual";
  }

}

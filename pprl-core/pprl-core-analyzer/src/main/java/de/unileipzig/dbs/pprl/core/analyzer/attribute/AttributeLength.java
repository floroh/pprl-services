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

import de.unileipzig.dbs.pprl.core.analyzer.results.Result;
import de.unileipzig.dbs.pprl.core.analyzer.results.ResultSet;
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.BitVector;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.IntColumn;
import tech.tablesaw.api.LongColumn;
import tech.tablesaw.api.Table;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Measures the length of the attribute values of each type
 * Empty or invalid attribute values are ignored
 */
public class AttributeLength extends AttributeAnalyzer {
  public static final String HEADER_ATTRIBUTE_LENGTH = "length";

  @Override
  public ResultSet analyze(Map<String, List<Attribute>> attributes) {
    ResultSet resultSet = getResultSet();
    resultSet.setDescription("Empty or invalid attribute values (e.g. null, NaN) are ignored. \n" +
      " For Bitvector attributes the cardinality is used as the length");
    for (Map.Entry<String, List<Attribute>> attribute : attributes.entrySet()) {
      DescriptiveStatistics stats = new DescriptiveStatistics();

      attribute.getValue()
        .forEach(attr -> {
          if (AttributeAvailability.isInvalidOrEmpty(attribute.getKey(), attr)) {
            return;
          }
          int len = attr.isType(BitVector.class) ? attr.getAs(BitVector.class)
            .getCardinality() : attr.getAsString()
            .length();
          stats.addValue(len);
        });
      if (stats.getN() == 0) {
        continue;
      }
      Result result = new Result();
      result.setParam(HEADER_ATTRIBUTE, attribute.getKey());
      addDescriptiveStatisticMetrics(result, stats,
        Arrays.asList("count", "median", "mean", "min", "max", "sd")
      );
      resultSet.addResult(result);

      Table lengthDistribution = Table.create(attribute.getKey())
        .addColumns(
          IntColumn.create(HEADER_ATTRIBUTE_LENGTH),
          LongColumn.create(HEADER_ABSOLUTE_FREQUENCY),
          DoubleColumn.create(HEADER_RELATIVE_FREQUENCY)
        );
      Arrays.stream(stats.getValues())
        .mapToObj(d -> (int) d)
        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
        .forEach((len, count) -> {
          lengthDistribution.intColumn(HEADER_ATTRIBUTE_LENGTH)
            .append(len);
          lengthDistribution.longColumn(HEADER_ABSOLUTE_FREQUENCY)
            .append(count);
          lengthDistribution.doubleColumn(HEADER_RELATIVE_FREQUENCY)
            .append((double) count / stats.getN());
        });
      resultSet.addAdditionalResult(lengthDistribution);
    }
    return resultSet;
  }
}

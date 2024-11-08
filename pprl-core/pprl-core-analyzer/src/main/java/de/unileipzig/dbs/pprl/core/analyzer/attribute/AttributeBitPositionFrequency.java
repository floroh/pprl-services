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
import tech.tablesaw.api.Table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Measures the frequency of the bit positions in Attributes of type BitVector
 * Empty or invalid attribute values are ignored
 */
public class AttributeBitPositionFrequency extends AttributeAnalyzer {

  public static final String HEADER_POSITION = "position";

  @Override
  public ResultSet analyze(Map<String, List<Attribute>> attributes) {
    ResultSet resultSet = getResultSet();
    resultSet.setDescription("Share of bit positions in (bitvector) attributes that are set to '1' \n");
    for (Map.Entry<String, List<Attribute>> attribute : attributes.entrySet()) {
      final List<Long> bitPositionCounter = new ArrayList<>();
      attribute.getValue()
        .forEach(attr -> {
          if (!attr.isType(BitVector.class)) {
            return;
          }
          BitVector bv = attr.getAs(BitVector.class);
          if (bitPositionCounter.isEmpty()) {
            bitPositionCounter.addAll(Collections.nCopies(bv.getLength(), 0L));
          }
          BitSet bs = bv.getBitSet();
          for (int i = 0; i < bv.getLength(); i++) {
            if (bs.get(i)) {
              bitPositionCounter.set(i, bitPositionCounter.get(i) + 1);
            }
          }
        });
      DescriptiveStatistics stats = new DescriptiveStatistics();
      int bvCount = attribute.getValue()
        .size();
      bitPositionCounter.forEach(l -> stats.addValue((double) l / bvCount));
      if (stats.getN() == 0) {
        continue;
      }

      Table bpRelFreq = Table.create(
        attribute.getKey(),
        IntColumn.create(HEADER_POSITION, IntStream.range(0, (int) stats.getN())),
        DoubleColumn.create(HEADER_RELATIVE_FREQUENCY, stats.getValues())
      );
      resultSet.addAdditionalResult(bpRelFreq);

      Result result = new Result();
      result.setParam(HEADER_ATTRIBUTE, attribute.getKey());
      addDescriptiveStatisticMetrics(result, stats,
        Arrays.asList("count", "median", "mean", "min", "max", "sd")
      );
      resultSet.addResult(result);
    }
    return resultSet;
  }
}

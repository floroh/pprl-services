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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Computes privacy metrics for Bloom Filter attributes
 */
public class AttributePrivacy extends AttributeAnalyzer {

  public static final String HEADER_POSITION = "position";
  public static final double LOG_2 = Math.log(2);

  @Override
  public ResultSet analyze(Map<String, List<Attribute>> attributes) {
    ResultSet resultSet = getResultSet();
    resultSet.setDescription("Privacy measures for Bloom Filter encodings where lower values (close to " +
            "zero) are better");
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

      long total = bitPositionCounter.stream().mapToLong(l -> l).sum();
      if (total == 0) {
        continue;
      }

      final List<Double> bitPositionShares = bitPositionCounter.stream()
              .map(count -> (double)count / total)
              .collect(Collectors.toList());

      final double perfectValue = (double) 1 / bitPositionCounter.size();
      final List<Double> sharesWithPerfectScore = prependToList(bitPositionShares, perfectValue);
      final List<Long> countsWithTotal = prependToList(bitPositionCounter, total);
      Table bpShares = Table.create(
              attribute.getKey(),
              IntColumn.create(HEADER_POSITION, IntStream.range(-1, bitPositionShares.size())),
              LongColumn.create("COUNT", countsWithTotal.stream().mapToLong(Long::longValue)),
              DoubleColumn.create(HEADER_RELATIVE_FREQUENCY, sharesWithPerfectScore)
      );
      resultSet.addAdditionalResult(bpShares);

      long validCount =
              attribute.getValue().stream()
                      .filter(v -> !AttributeAvailability.isInvalidOrEmpty(attribute.getKey(), v))
                      .count();
      Result result = new Result();
      result.setParam(HEADER_ATTRIBUTE, attribute.getKey());
      result.addMetric("Count", createBigDecimal(validCount));
      result.addMetric("Shannon-Entropy", createBigDecimal(computeShannonEntropy(bitPositionShares)));
      result.addMetric("Norm-Shannon-Entropy",
              createBigDecimal(computeNormalizedShannonEntropy(bitPositionShares)));
      result.addMetric("Gini", createBigDecimal(computeGiniCoefficient(bitPositionCounter)));
      result.addMetric("JSD", createBigDecimal(computeJensenShannonDistance(bitPositionShares)));
      resultSet.addResult(result);
    }
    return resultSet;
  }

  private BigDecimal createBigDecimal(double value) {
    if (Double.isNaN(value)) {
      return BigDecimal.valueOf(-1);
    }
    return BigDecimal.valueOf(value);
  }

  private <T> List<T> prependToList(List<T> list, T firstElement) {
    ArrayList<T> newList = new ArrayList<>();
    newList.add(firstElement);
    newList.addAll(list);
    return newList;
  }

  private static double computeJensenShannonDistance(List<Double> probabilities) {
    int m = probabilities.size();
    double mInv = 1.0 / m;

    double kldPM = probabilities.stream()
            .mapToDouble(p -> p == 0 ? 0.0 : Math.log(mInv / (0.5 * (p + mInv))) / LOG_2)
            .sum()
            / m;
    double kldQM = probabilities.stream()
            .mapToDouble(p -> p == 0 ? 0.0 : p * Math.log(p / (0.5 * (p + mInv))) / LOG_2)
            .sum();
    double jsd = 0.5 * kldPM + 0.5 * kldQM;
    return Math.sqrt(jsd);
  }

  private static double computeGiniCoefficient(List<Long> counts) {
    long total = counts.stream().mapToLong(l -> l).sum();
    long diffSum = 0;
    int m = counts.size();
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < m; j++) {
        diffSum += Math.abs(counts.get(i) - counts.get(j));
      }
    }
    return (double)diffSum / (2 * m * total);
  }
  private static double computeNormalizedShannonEntropy(List<Double> probabilities) {
    double maxEntropy = Math.log(probabilities.size()) / LOG_2;
    return 1 - computeShannonEntropy(probabilities) / maxEntropy;
  }

  private static double computeShannonEntropy(List<Double> probabilities) {
    double tmp = probabilities.stream()
            .mapToDouble(p -> p == 0 ? 0.0 : p * Math.log(p) / LOG_2)
            .sum();
    return -tmp;
  }
}

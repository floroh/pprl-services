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

import de.unileipzig.dbs.pprl.core.analyzer.cluster.data.AttributePairDiff;
import de.unileipzig.dbs.pprl.core.analyzer.cluster.data.CustomDiff;
import de.unileipzig.dbs.pprl.core.analyzer.cluster.data.CustomOperation;
import de.unileipzig.dbs.pprl.core.analyzer.cluster.data.Pair;
import de.unileipzig.dbs.pprl.core.analyzer.results.Result;
import de.unileipzig.dbs.pprl.core.analyzer.results.ResultSet;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Analyze the cluster of records belonging to the same real world entity
 * by the differences between pairs of these records
 */
public class ClusterPairwiseDiff extends ClusterPairwiseAnalyzer {

  public static final String LEFT_ID = "ID0";
  public static final String RIGHT_ID = "ID1";
  private static final String PREFIX_PAIRS = "Pairs_";
  private static final String PREFIX_MASKED = "Masked_";
  private static final String PREFIX_BY_TYPE = "ByType_";
  private static final String FULL_RECORD = "full record";
  private static final double DEFAULT_MIN_EQUAL_CHARACTER_SHARE = 0.5;
  private static final int DEFAULT_MAX_LENGTH_OF_DIFF = 3;
  private static final boolean DEFAULT_INCLUDE_ATTRIBUTE_PAIRS = true;
  private static final boolean DEFAULT_INCLUDE_MASKED_DIFFS = true;
  private static final boolean DEFAULT_INCLUDE_DIFFS_BY_TYPE = true;

  /**
   * Minimal share of equal characters per attribute value pair
   * prohibits leaks of large parts of attributes values (e.g. ?[ei/üll]?? for "Meier" and "Müller")
   */
  private double minEqualCharacterShare;

  /**
   * Maximal length of a attribute segment to be included in the MASKED or BYTYPE output
   * prohibits leaks of substrings from long attribute values (e.g. ????[ashi]?? is probably "kardashian")
   */
  private int maxLengthOfDiff;

  /**
   * Include differing attribute pairs in plain text in the result
   */
  private boolean includeAttributePairs;

  /**
   * Include masked differences between differing attribute values in the result
   */
  private boolean includeMaskedDiffs;

  /**
   * Include differing substrings with the type of the difference (e.g. replacement, swap) in the result
   */
  private boolean includeDiffsByType;

  public ClusterPairwiseDiff() {
    this.minEqualCharacterShare = DEFAULT_MIN_EQUAL_CHARACTER_SHARE;
    this.maxLengthOfDiff = DEFAULT_MAX_LENGTH_OF_DIFF;
    this.includeAttributePairs = DEFAULT_INCLUDE_ATTRIBUTE_PAIRS;
    this.includeMaskedDiffs = DEFAULT_INCLUDE_MASKED_DIFFS;
    this.includeDiffsByType = DEFAULT_INCLUDE_DIFFS_BY_TYPE;
    logger.info("Initialized: " + this);
  }

  @Override
  public ResultSet analyze(Map<String, List<Record>> clusters) {
    ResultSet resultSet = getResultSet();
    resultSet.setDescription(buildDescription());

    Map<String, DescriptiveStatistics> stats = new HashMap<>();
    stats.put(FULL_RECORD, new DescriptiveStatistics());

    Map<String, List<AttributePairDiff>> differingAttributePairs = new HashMap<>();

    for (List<Record> cluster : clusters.values()) {
      List<Pair<Record>> recordPairs = buildRecordPairs(cluster);
      //TODO Normalize on number of pairs to prevent disproportional influence of large clusters?
      for (Pair<Record> recordPair : recordPairs) {
        double recordDistance = 0;

        for (AttributePairDiff apd : buildAttributePairDiffs(recordPair)) {
          String attributeName = apd.getAttributeName();
          if (!stats.containsKey(attributeName)) {
            stats.put(attributeName, new DescriptiveStatistics());
          }
          double attrDistance = apd.getDistance();
          if (attrDistance > 0) {
            if (!differingAttributePairs.containsKey(attributeName)) {
              differingAttributePairs.put(attributeName, new ArrayList<>());
            }
            differingAttributePairs.get(attributeName).add(apd);
          }
          stats.get(attributeName).addValue(attrDistance);
          recordDistance += attrDistance;
        }
        stats.get(FULL_RECORD).addValue(recordDistance);
      }
    }

    List<String> attributeNames = stats.keySet()
      .stream()
      .sorted()
      .collect(Collectors.toList());

    for (String attributeName : attributeNames) {
      Result result = new Result();
      result.setParam(HEADER_ATTRIBUTE, attributeName);
      addDescriptiveStatisticMetrics(result, stats.get(attributeName),
        Arrays.asList("count", "median", "mean", "min", "max", "sd")
      );
      resultSet.addResult(result);
    }

    buildDifferingAttributePairsResults(differingAttributePairs).forEach(resultSet::addAdditionalResult);

    return resultSet;
  }

  protected List<AttributePairDiff> buildAttributePairDiffs(Pair<Record> recordPair) {
    return super.buildAttributePairs(recordPair).stream()
      .map(AttributePairDiff::new)
      .collect(Collectors.toList());
  }

  private String buildDescription() {
    StringBuilder sb = new StringBuilder();
    sb.append("""
      All attribute values of the same type are compared within each cluster on the basis \
      of the Levenshtein distance.
      Additionally the attribute distances are accumulated to the record \
      distance.
      """);
    if (includeAttributePairs || includeMaskedDiffs || includeDiffsByType) {
      sb.append(
        "The output directory contains files for each attribute type with the following filename " +
          "prefixes:\n");
      if (includeAttributePairs) {
        sb.append("\t " + PREFIX_PAIRS + " : contains all differing attribute value pairs in plain text\n");
      }
      if (includeMaskedDiffs) {
        sb.append("\t " + PREFIX_MASKED +
          " : contains the masked differences between differing attribute value pairs\n");
      }
      if (includeDiffsByType) {
        sb.append("\t " + PREFIX_BY_TYPE + " : contains the differing substrings and the type of difference " +
          "(REPLACEMENT, SWAP, INSERTION)");
      }
    }
    return sb.toString();
  }

  private List<Table> buildDifferingAttributePairsResults(
    Map<String, List<AttributePairDiff>> allDifferingAttributePairs) {
    List<Table> results = new ArrayList<>();
    for (Map.Entry<String, List<AttributePairDiff>> attributePairs : allDifferingAttributePairs.entrySet()) {
      logger.info("Number of differing attributes for type " + attributePairs.getKey() + ": " +
        attributePairs.getValue()
          .size());

      if (includeAttributePairs) {
        results.add(buildAttributePairTable(attributePairs.getKey(), attributePairs.getValue()));
      }

      List<List<CustomDiff>> diffs = getFilteredDiffs(attributePairs.getValue());
      logger.info("Number of differing attributes after privacy filtering: " + diffs.size());

      if (includeMaskedDiffs) {
        results.add(buildMaskedDiffTable(attributePairs.getKey(), diffs));
      }
      if (includeDiffsByType) {
        results.add(buildDiffsByTypeTable(attributePairs.getKey(), diffs));
      }
    }
    return results;
  }

  private List<List<CustomDiff>> getFilteredDiffs(List<AttributePairDiff> attributePairs) {
    return attributePairs.stream()
      .filter(ap -> ap.getEqualCharactersShare() > minEqualCharacterShare)
      .map(AttributePairDiff::getDiffs)
      .map(ds -> ds.stream()
        .filter(d -> d.getS0().length() <= maxLengthOfDiff ||
          d.getS1().length() <= maxLengthOfDiff)
        .collect(Collectors.toList()))
      .collect(Collectors.toList());
  }

  private Table buildAttributePairTable(String attributeName, List<AttributePairDiff> attributePairs) {
    StringColumn colAttr0 = StringColumn.create("attr0");
    StringColumn colAttr1 = StringColumn.create("attr1");
    attributePairs.forEach(ap -> {
      colAttr0.append(ap.getV0().getAsString());
      colAttr1.append(ap.getV1().getAsString());
    });
    return Table.create(PREFIX_PAIRS + attributeName, colAttr0, colAttr1);
  }

  private Table buildMaskedDiffTable(String attributeName, List<List<CustomDiff>> diffs) {
    StringColumn colDiff = StringColumn.create("attributeDiff");
    diffs.stream()
      .map(ds -> ds.stream()
        .map(CustomDiff::toString)
        .collect(Collectors.joining()))
      .filter(s -> s.matches(".*[^?]+.*")) // Remove strings containing only "?" chars
      .forEach(colDiff::append);
    return Table.create(PREFIX_MASKED + attributeName, colDiff);
  }

  private Table buildDiffsByTypeTable(String attributeName, List<List<CustomDiff>> diffs) {
    StringColumn colType = StringColumn.create("type");
    StringColumn colString0 = StringColumn.create("string0");
    StringColumn colString1 = StringColumn.create("string1");
    diffs.stream()
      .flatMap(List::stream)
      .filter(diff -> diff.getOperation() != CustomOperation.EQUAL)
      .forEach(diff -> {
        colType.append(diff.getOperation().toString());
        colString0.append(diff.getS0());
        colString1.append(diff.getS1());
      });
    return Table.create(PREFIX_BY_TYPE + attributeName, colType, colString0, colString1);
  }

  public double getMinEqualCharacterShare() {
    return minEqualCharacterShare;
  }

  public void setMinEqualCharacterShare(double minEqualCharacterShare) {
    this.minEqualCharacterShare = minEqualCharacterShare;
  }

  public int getMaxLengthOfDiff() {
    return maxLengthOfDiff;
  }

  public void setMaxLengthOfDiff(int maxLengthOfDiff) {
    this.maxLengthOfDiff = maxLengthOfDiff;
  }

  public boolean isIncludeAttributePairs() {
    return includeAttributePairs;
  }

  public void setIncludeAttributePairs(boolean includeAttributePairs) {
    this.includeAttributePairs = includeAttributePairs;
  }

  public boolean isIncludeMaskedDiffs() {
    return includeMaskedDiffs;
  }

  public void setIncludeMaskedDiffs(boolean includeMaskedDiffs) {
    this.includeMaskedDiffs = includeMaskedDiffs;
  }

  public boolean isIncludeDiffsByType() {
    return includeDiffsByType;
  }

  public void setIncludeDiffsByType(boolean includeDiffsByType) {
    this.includeDiffsByType = includeDiffsByType;
  }

  @Override
  public String toString() {
    return "ClusterPairwiseDiff{" + "minEqualCharacterShare=" + minEqualCharacterShare +
      ", maxLengthOfDiff=" + maxLengthOfDiff + ", includeAttributePairs=" + includeAttributePairs +
      ", includeMaskedDiffs=" + includeMaskedDiffs + ", includeDiffsByType=" + includeDiffsByType + '}';
  }
}

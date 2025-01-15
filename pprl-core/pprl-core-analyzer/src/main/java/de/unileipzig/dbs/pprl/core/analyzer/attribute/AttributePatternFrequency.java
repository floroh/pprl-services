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
import tech.tablesaw.api.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Find frequencies of attribute values matching certain regex patterns
 */
public class AttributePatternFrequency extends AttributeFrequencyAnalyzer {

  public static final String HEADER_PATTERN = "pattern";

  /**
   * Regex patterns for specific attribute types to match with
   */
  private Map<String, List<String>> patterns;

  public AttributePatternFrequency() {
    super();
    this.patterns = new HashMap<>();
    logger.info("Initialized: " + this);
  }

  @Override
  public ResultSet analyze(Map<String, List<Attribute>> attributes) {
    ResultSet resultSet = new ResultSet(getName());
    resultSet.setDescription(buildDescription());

    List<String> attributeNames = attributes.keySet()
      .stream()
      .sorted()
      .collect(Collectors.toList());

    for (String attributeName : attributeNames) {
      List<Attribute> curAttributes = attributes.get(attributeName);
      List<ValueFrequency> valueFrequencies = getSortedValueFrequencies(curAttributes, attributeName);

      for (ValueFrequency valueFrequency : valueFrequencies) {
        Result result = new Result();
        result.setParam(HEADER_ATTRIBUTE, attributeName);
        result.setParam(HEADER_PATTERN, valueFrequency.getValue());
        result.addMetric(
          HEADER_ABSOLUTE_FREQUENCY,
          BigDecimal.valueOf(valueFrequency.getAbsoluteFrequency())
        );
        result.addMetric(
          HEADER_RELATIVE_FREQUENCY,
          BigDecimal.valueOf((double) valueFrequency.getAbsoluteFrequency() / curAttributes.size())
        );
        resultSet.addResult(result);
      }
      if (!valueFrequencies.isEmpty()) {
        Table subResult = buildFrequencyResult(attributeName, valueFrequencies, curAttributes.size());
        resultSet.addAdditionalResult(subResult);
      }
    }
    return resultSet;
  }

  @Override
  protected Collection<String> getValues(Attribute attribute, String attributeName) {
    if (AttributeAvailability.isInvalidOrEmpty(attributeName, attribute)) {
      return Collections.emptyList();
    }
    String value = attribute.getAsString();
    if (toLowerCase) {
      value = value.toLowerCase();
    }
    final String fValue = value;
    if (patterns.containsKey(attributeName)) {
      return patterns.get(attributeName)
        .stream()
        .filter(pattern -> {
          String patternProc = toLowerCase ? pattern.toLowerCase() : pattern;
          Matcher m = Pattern.compile(patternProc)
            .matcher(fValue);
//								boolean match = m.find();
          boolean match = m.matches();
          if (match) {
//									logger.debug(pattern + " -> " + fValue);
            return true;
          } else {
            return false;
          }
        })
        .collect(Collectors.toList());
    }
    return Collections.emptyList();
  }

  @Override
  protected String buildDescription() {
    return "Frequencies of attribute values that match a regex pattern";
  }

  public Map<String, List<String>> getPatterns() {
    return patterns;
  }

  public void setPatterns(Map<String, List<String>> patterns) {
    this.patterns = patterns;
  }

  public void addPattern(String attributeName, String pattern) {
    if (!patterns.containsKey(attributeName)) {
      patterns.put(attributeName, new ArrayList<>());
    }
    patterns.get(attributeName)
      .add(pattern);
  }

  public void addPatterns(String attributeName, List<String> newPatterns) {
    if (!patterns.containsKey(attributeName)) {
      patterns.put(attributeName, new ArrayList<>());
    }
    patterns.get(attributeName)
      .addAll(newPatterns);
  }

  @Override
  public String toString() {
    return "AttributePatternFrequency{" + "patterns=" + patterns + ", maxNumber=" + maxNumber +
      ", minCount=" + minCount + ", cumShares=" + cumShares + ", toLowerCase=" + toLowerCase + '}';
  }
}

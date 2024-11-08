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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Measure the share of attribute values that are valid, invalid (e.g. null, NaN) or empty
 * for each attribute type
 */
public class AttributeAvailability extends AttributeAnalyzer {
  public static final String[] INVALID_STRINGS = {"null", "NULL", "Null", "NaN"};

  public static final String VALID = "valid";
  public static final String INVALID = "invalid";
  public static final String EMPTY = "empty";
  public static final String MISSING = "missing";

  @Override
  public ResultSet analyze(Map<String, List<Attribute>> attributes) {
    ResultSet resultSet = getResultSet();
    resultSet.setDescription(
      "Share of attribute values that are valid, invalid (e.g. null, NaN), empty or missing");
    for (Map.Entry<String, List<Attribute>> attribute : attributes.entrySet()) {
      int empty = 0;
      int valid = 0;
      int invalid = 0;
      for (Attribute attr : attribute.getValue()) {
        if (attr.isEmpty()) {
          empty++;
        } else if (isInvalid(attribute.getKey(), attr)) {
          invalid++;
        } else {
          valid++;
        }
      }
      int missing = recordCount - empty - valid - invalid;

      Result result = new Result();
      result.setParam(HEADER_ATTRIBUTE, attribute.getKey());
      List<BigDecimal> values = Stream.of(valid, invalid, empty, missing)
        .map(i -> (double) i / recordCount)
        .map(BigDecimal::valueOf)
        .collect(Collectors.toList());
      result.addMetric(VALID, values.get(0));
      result.addMetric(INVALID, values.get(1));
      result.addMetric(EMPTY, values.get(2));
      result.addMetric(MISSING, values.get(3));
      resultSet.addResult(result);
    }
    return resultSet;
  }

  //TODO Make this class abstract and move the method isValid() to the subclasses
  public static boolean isInvalid(String attributeName, Attribute attribute) {
    if (attribute.isString()) {
      return Arrays.asList(INVALID_STRINGS)
        .contains(attribute.getAsString());
    }
    return false;
  }

  public static boolean isInvalidOrEmpty(String attributeName, Attribute attribute) {
    return attribute.isEmpty() || isInvalid(attributeName, attribute);
  }
}

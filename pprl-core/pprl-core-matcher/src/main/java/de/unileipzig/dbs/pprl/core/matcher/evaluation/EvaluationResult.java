/*
 * Copyright © 2018 - 2020 Leipzig University (Database Research Group)
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

package de.unileipzig.dbs.pprl.core.matcher.evaluation;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EvaluationResult {

  protected Map<String, String> params;
  protected Map<String, BigDecimal> metrics;

  public EvaluationResult() {
    params = new HashMap<>();
    metrics = new HashMap<>();
  }

  public void setParam(String key, String val) {
    params.put(key, val);
  }

  public Map<String, String> getParams() {
    return params;
  }

  public void setParams(Map<String, String> params) {
    this.params = params;
  }

  public Map<String, BigDecimal> getMetrics() {
    return metrics;
  }

  public void setMetrics(Map<String, BigDecimal> metrics) {
    this.metrics = metrics;
  }

  public void addMetric(String name, BigDecimal value) {
    this.metrics.put(name, value);
  }

  public static String round(Double d) {
    return String.format(Locale.ENGLISH, "%.4f", d);
  }

  @Override
  public String toString() {
    return "EvaluationResult{" +
      "params=" + params +
      ", metrics=" + metrics +
      '}';
  }
}

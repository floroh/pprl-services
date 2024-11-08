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

package de.unileipzig.dbs.pprl.core.common.preprocessing;

import java.util.Arrays;
import java.util.List;

public class StringAttributeSplitter implements AttributeSplitter<String, String> {
  public static final String DEFAULT_DELIMITERS = "[ -.]";

  private String delimiters = DEFAULT_DELIMITERS;

  public StringAttributeSplitter(String delimiters) {
    this.delimiters = delimiters;
  }

  public StringAttributeSplitter() {
  }

  @Override
  public List<String> preprocess(String in) {
    String[] parts = in.split(delimiters);
    return Arrays.asList(parts);
  }

  @Override
  public Class<String> getInputClass() {
    return String.class;
  }

  @Override
  public Class<String> getOutputClass() {
    return String.class;
  }

  public String getDelimiters() {
    return delimiters;
  }
}

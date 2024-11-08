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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StringSubstitution extends StringAttributePreprocessor {
  private static final String[] INPUT_STRINGS = new String[] {
    "\u00e4", "\u00f6", "\u00fc", // "ä", "ö", "ü"
    "\u00c4", "\u00d6", "\u00dc", // "Ä", "Ö", "Ü"
    "\u00df",                      // "ß"
  };

  private static final String[] REPLACEMENTS = new String[] {
    "ae", "oe", "ue", "Ae", "Oe", "Ue", "ss",
  };

  private List<String> strings = Arrays.asList(INPUT_STRINGS);
  private List<String> replacements = Arrays.asList(REPLACEMENTS);

  private List<Pattern> patterns;

  public StringSubstitution() {
    patterns = strings.stream()
      .map(Pattern::compile)
      .collect(Collectors.toList());
  }

  @Override
  public String preprocess(String value) {
    for (int i = 0; i < patterns.size(); i++) {
      value = patterns.get(i)
        .matcher(value)
        .replaceAll(replacements.get(i));
    }
    return value;
  }

  public List<String> getStrings() {
    return strings;
  }

  public List<String> getReplacements() {
    return replacements;
  }
}

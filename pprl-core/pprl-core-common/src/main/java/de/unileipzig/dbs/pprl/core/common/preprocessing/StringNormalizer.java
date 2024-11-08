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

import org.apache.commons.lang3.StringUtils;

public class StringNormalizer extends StringAttributePreprocessor {
  private boolean trim = true;
  private boolean deleteWhitespace = false;
  private boolean toLowerCase = true;
  private boolean stripAccents = true;

  public StringNormalizer(boolean trim, boolean deleteWhitespace, boolean toLowerCase, boolean stripAccents) {
    this.trim = trim;
    this.deleteWhitespace = deleteWhitespace;
    this.toLowerCase = toLowerCase;
    this.stripAccents = stripAccents;
  }

  public StringNormalizer() {
  }

  @Override
  public String preprocess(String attribute) {
    String cleanedValue = attribute;

    if (trim) {
      cleanedValue = cleanedValue.trim();
    }
    if (deleteWhitespace) {
      cleanedValue = cleanedValue.replaceAll("\\s+", "");
    }
    if (toLowerCase) {
      cleanedValue = cleanedValue.toLowerCase();
    }
    if (stripAccents) {
      cleanedValue = StringUtils.stripAccents(cleanedValue);
    }

    return cleanedValue;
  }

  public boolean isTrim() {
    return trim;
  }

  public StringNormalizer setTrim(boolean trim) {
    this.trim = trim;
    return this;
  }

  public boolean isDeleteWhitespace() {
    return deleteWhitespace;
  }

  public StringNormalizer setDeleteWhitespace(boolean deleteWhitespace) {
    this.deleteWhitespace = deleteWhitespace;
    return this;
  }

  public boolean isToLowerCase() {
    return toLowerCase;
  }

  public StringNormalizer setToLowerCase(boolean toLowerCase) {
    this.toLowerCase = toLowerCase;
    return this;
  }

  public boolean isStripAccents() {
    return stripAccents;
  }

  public StringNormalizer setStripAccents(boolean stripAccents) {
    this.stripAccents = stripAccents;
    return this;
  }
}
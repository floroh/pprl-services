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

package de.unileipzig.dbs.pprl.service.dataowner.modifier.attribute;

import lombok.extern.log4j.Log4j2;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Log4j2
public class DateTypoModifier implements AttributeModifier<String> {
  public static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";

  public static final String TAG_POSTFIX = "_DATETYPO";

  /**
   * Pattern of input and output dates
   */
  private String datePattern = DEFAULT_DATE_PATTERN;

  private boolean autoInputPatternDetection = true;

  /**
   * Concatenated symbols of components of the date [dMy], that might be modified.
   * e.g. to modify the year only, select "y"
   */
  private String possibleComponents;

  private DateTimeFormatter dateFormatter;

  private long seed;

  private Random r;

  public DateTypoModifier(String possibleComponents) {
    this(possibleComponents, 123);
  }

  public DateTypoModifier(String possibleComponents, long seed) {
    this.possibleComponents = possibleComponents;
    this.seed = seed;
  }

  private DateTypoModifier() {
  }

  @Override
  public String modify(String in) {
    init();
    LocalDate date;

    String inputPattern = datePattern;
    if (autoInputPatternDetection) {
      inputPattern = detectInputPattern(in);
    }

    if (!inputPattern.equals(datePattern)) {
      log.debug("Changing date input pattern {} to {}", datePattern, inputPattern);
      datePattern = inputPattern;
      dateFormatter = DateTimeFormatter.ofPattern(datePattern);
    }

    if (datePattern.equals("yyyy")) {
      date = LocalDate.of(Integer.parseInt(in), 1, 1);
    } else {
      date = LocalDate.parse(in, dateFormatter);
    }

    int component = r.nextInt(possibleComponents.length());
    date = switch (possibleComponents.charAt(component)) {
      case 'd' -> date.withDayOfMonth(getReplacementNumber(date.getDayOfMonth(), 1, 28));
      case 'M' -> date.withMonth(getReplacementNumber(date.getMonthValue(), 1, 12));
      case 'y' -> date.withYear(getReplacementNumber(date.getYear(), 1960, 2000));
      default -> throw new RuntimeException("unknown date component");
    };
    return dateFormatter.format(date);
  }

  private void init() {
    if (dateFormatter == null) {
      dateFormatter = DateTimeFormatter.ofPattern(datePattern);
    }
    if (r == null) {
      r = new Random(seed);
    }
  }

  private String detectInputPattern(String input) {
    // Check for yyyy-MM-dd pattern
    if (input.matches("\\d{4}-\\d{2}-\\d{2}")) {
      return "yyyy-MM-dd";
    }
    // Check for dd.MM.yyyy pattern
    else if (input.matches("\\d{2}\\.\\d{2}\\.\\d{4}")) {
      return "dd.MM.yyyy";
    }
    return datePattern;
  }

  @Override
  public String getTagPostFix() {
    return TAG_POSTFIX + "_" + possibleComponents;
  }

  private int getReplacementNumber(int old, int min, int max) {
    int c = getRandomNumber(min, max);
    while (c == old) {
      c = getRandomNumber(min, max);
    }
    return c;
  }

  private int getRandomNumber(int min, int max) {
    return min + r.nextInt(max - min);
  }

  public String getDatePattern() {
    return datePattern;
  }

  public DateTypoModifier setDatePattern(String datePattern) {
    this.datePattern = datePattern;
    this.dateFormatter = null;
    return this;
  }

  public String getPossibleComponents() {
    return possibleComponents;
  }

  public void setPossibleComponents(String possibleComponents) {
    this.possibleComponents = possibleComponents;
  }

  public long getSeed() {
    return seed;
  }

  public void setSeed(long seed) {
    this.seed = seed;
  }
}

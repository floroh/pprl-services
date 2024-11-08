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

package de.unileipzig.dbs.pprl.core.encoder.blocking;

import de.unileipzig.dbs.pprl.core.common.HashUtils;
import de.unileipzig.dbs.pprl.core.common.factories.BlockingKeyFactory;
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.BlockingKey;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static java.time.temporal.ChronoUnit.DAYS;

public class HashedDateOfBirth extends SingleAttributeBlocker {
  public static final String DEFAULT_OUTPUT_DATE_PATTERN = "yyyyMMdd";
  public static final String DEFAULT_OUTPUT_DATE_START = "19000101";
  public static final String DEFAULT_OUTPUT_DATE_END = "19000201";
  public static final boolean DEFAULT_USE_SWITCHED_DAY_AND_MONTH = false;

  private String inputDatePattern;
  private String outputDatePattern = DEFAULT_OUTPUT_DATE_PATTERN;
  private String outputStartDateString = DEFAULT_OUTPUT_DATE_START;
  private String outputEndDateString = DEFAULT_OUTPUT_DATE_END;
  private boolean useSwitchedDayAndMonth = DEFAULT_USE_SWITCHED_DAY_AND_MONTH;

  private Collection<DateTimeFormatter> inputFormatters;
  private DateTimeFormatter outputFormatter;
  private LocalDate outputStartDate;
  private long outputDateRange;

  public HashedDateOfBirth(String id, String attributeKey, String inputDatePattern, String outputDatePattern,
    String outputStartDateString, String outputEndDateString, boolean useSwitchedDayAndMonth) {
    super(id, attributeKey);
    this.inputDatePattern = inputDatePattern;
    this.outputDatePattern = outputDatePattern;
    this.outputStartDateString = outputStartDateString;
    this.outputEndDateString = outputEndDateString;
    this.useSwitchedDayAndMonth = useSwitchedDayAndMonth;
    parseFormatters();
  }

  public HashedDateOfBirth(String id, String attributeKey, String inputDatePattern,
    boolean useSwitchedDayAndMonth) {
    super(id, attributeKey);
    this.inputDatePattern = inputDatePattern;
    this.useSwitchedDayAndMonth = useSwitchedDayAndMonth;
  }

  private HashedDateOfBirth() {
    super();
  }

  @Override
  public Set<BlockingKey> extract(Attribute attribute) {
    if (inputFormatters == null) {
      parseFormatters();
    }
    String dob = attribute.getAsString();
    Set<BlockingKey> keys = new HashSet<>();
    for (DateTimeFormatter formatter : inputFormatters) {
      try {
        LocalDate date = LocalDate.parse(dob, formatter);
        BlockingKey bk = BlockingKeyFactory.getBlockingKey(id, getBlockingKeyValueFromString(outputFormatter.format(date)));
        keys.add(bk);
      } catch (DateTimeParseException e) {
        // Ignore parsing errors that could be caused by switching date and month
      }
    }
    return keys;
  }

  //TODO Use custom Jackson deserializer instead
  private void parseFormatters() {
    this.inputFormatters = new ArrayList<>();
    this.inputFormatters.add(DateTimeFormatter.ofPattern(inputDatePattern));
    if (useSwitchedDayAndMonth) {
      inputFormatters.add(DateTimeFormatter.ofPattern(switchDayAndMonth(inputDatePattern)));
    }
    this.outputFormatter = DateTimeFormatter.ofPattern(outputDatePattern);
    this.outputStartDate = LocalDate.parse(outputStartDateString, outputFormatter);
    LocalDate outputEndDate = LocalDate.parse(outputEndDateString, outputFormatter);
    this.outputDateRange = DAYS.between(outputStartDate, outputEndDate);
  }

  public String getInputDatePattern() {
    return inputDatePattern;
  }

  public String getOutputDatePattern() {
    return outputDatePattern;
  }

  public String getOutputStartDateString() {
    return outputStartDateString;
  }

  public String getOutputEndDateString() {
    return outputEndDateString;
  }

  public boolean isUseSwitchedDayAndMonth() {
    return useSwitchedDayAndMonth;
  }

  private String getBlockingKeyValueFromString(String value) {
    int hash = Math.abs(HashUtils.getSHA(value));
    LocalDate outputDate = outputStartDate.plusDays(hash % outputDateRange);
    return outputFormatter.format(outputDate);
  }

  private String switchDayAndMonth(String datePattern) {
    datePattern = datePattern.replace('M', '@');
    datePattern = datePattern.replace('d', 'M');
    datePattern = datePattern.replace('@', 'd');
    return datePattern;
  }

  @Override
  public String toString() {
    return "HashedDateOfBirth{" + "id='" + id + '\'' + ", attributeKey='" + attributeKey + '\'' +
      ", inputDatePattern='" + inputDatePattern + '\'' + ", outputDatePattern='" + outputDatePattern + '\'' +
      ", outputStartDateString='" + outputStartDateString + '\'' + ", outputEndDateString='" +
      outputEndDateString + '\'' + ", useSwitchedDayAndMonth=" + useSwitchedDayAndMonth + '}';
  }
}

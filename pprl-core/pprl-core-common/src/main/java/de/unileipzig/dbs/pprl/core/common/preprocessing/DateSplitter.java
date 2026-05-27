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

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.unileipzig.dbs.pprl.core.common.factories.AttributeFactory;
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.impl.PersonalAttributeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DateSplitter implements RecordPreprocessor {

  private static final Logger log = LoggerFactory.getLogger(DateSplitter.class);
  private String attributeId = PersonalAttributeType.DATEOFBIRTH.name();

  private static final Map<String, String> componentNames = Map.of(
    "d", PersonalAttributeType.DAYOFBIRTH.name(),
    "M", PersonalAttributeType.MONTHOFBIRTH.name(),
    "y", PersonalAttributeType.YEAROFBIRTH.name()
  );

//  private String inputDatePattern = "dd.MM.yyyy";
  private String inputDatePattern = "yyyy-MM-dd";

  private boolean keepFullDate = false;

  private boolean inPlace = false;

  @JsonIgnore
  private DateTimeFormatter dateFormatter;

  public DateSplitter() {
  }

  public DateSplitter(boolean keepFullDate) {
    this.keepFullDate = keepFullDate;
  }

  public DateSplitter(String attributeId, String inputDatePattern) {
    this.attributeId = attributeId;
    this.inputDatePattern = inputDatePattern;
  }

  @Override
  public Record preprocess(Record in) {
    if (dateFormatter == null) {
      this.dateFormatter = DateTimeFormatter.ofPattern(inputDatePattern);
    }
    Record out = inPlace ? in : in.duplicate();
    Optional<Attribute> optionalAttribute = in.getAttribute(attributeId);
    if (optionalAttribute.isEmpty()) {
//      if (areDateComponentsAvailable(in)) {
        return out;
//      } else {
//        throw new RuntimeException("Missing attribute: " + attributeId);
//      }
    }
    Attribute attribute = optionalAttribute.get();

    try {
      LocalDate date = LocalDate.parse(attribute.getAsString(), dateFormatter);
      addDateComponentAttribute(out, "d", date.getDayOfMonth());
      addDateComponentAttribute(out, "M", date.getMonthValue());
      addDateComponentAttribute(out, "y", date.getYear());
    } catch (DateTimeParseException e) {
      String[] components = attribute.getAsString().split("\\.");
      if (components.length == 1) {
        // Handle YYYYMMDD format
        String dateString = attribute.getAsString();
        if (dateString.length() == 8) {
          String year = dateString.substring(0, 4);
          String month = dateString.substring(4, 6);
          String day = dateString.substring(6, 8);
          components = new String[]{day, month, year};
        } else {
//          log.warn("Could not parse and split date of birth.");
          return out;
        }
      }
      addDateComponentAttribute(out, "d", Integer.parseInt(components[0]));
      addDateComponentAttribute(out, "M", Integer.parseInt(components[1]));
      addDateComponentAttribute(out, "y", Integer.parseInt(components[2]));
//      throw new RuntimeException("Could not parse input date: " + attribute.getAsString());
    }
    if (!keepFullDate) {
      out.removeAttribute(attributeId);
    }
    return out;
  }

  private boolean areDateComponentsAvailable(Record record) {
    for (String name : componentNames.values()) {
      if (record.getAttribute(name).isEmpty()) {
        return false;
      }
    }
    return true;
  }

  private void addDateComponentAttribute(Record record, String componentKey, int value) {
    String valueString = String.valueOf(value);
    if (List.of("d", "M").contains(componentKey) && valueString.length() == 1) {
      valueString = "0" + valueString;
    }
    Attribute attribute = AttributeFactory.getAttribute(valueString);
    record.setAttribute(componentNames.get(componentKey), attribute);
  }

  public String getAttributeId() {
    return attributeId;
  }

  public void setAttributeId(String attributeId) {
    this.attributeId = attributeId;
  }

  public boolean isKeepFullDate() {
    return keepFullDate;
  }

  public void setKeepFullDate(boolean keepFullDate) {
    this.keepFullDate = keepFullDate;
  }

  public boolean isInPlace() {
    return inPlace;
  }

  public void setInPlace(boolean inPlace) {
    this.inPlace = inPlace;
  }

  public String getInputDatePattern() {
    return inputDatePattern;
  }

  public void setInputDatePattern(String inputDatePattern) {
    this.inputDatePattern = inputDatePattern;
  }

  @Override
  public String toString() {
    return "DateSplitter{" +
            "attributeId='" + attributeId + '\'' +
            ", inputDatePattern='" + inputDatePattern + '\'' +
            ", keepFullDate=" + keepFullDate +
            ", inPlace=" + inPlace +
            '}';
  }
}

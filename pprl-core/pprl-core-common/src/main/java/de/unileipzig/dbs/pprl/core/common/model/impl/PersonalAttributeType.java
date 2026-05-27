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

package de.unileipzig.dbs.pprl.core.common.model.impl;

import java.util.*;


public enum PersonalAttributeType {
  FIRSTNAME,
  MIDDLENAME,
  LASTNAME,
  NAME_SUFFIX,
  NAMEATBIRTH,
  DATEOFBIRTH,
  DAYOFBIRTH,
  MONTHOFBIRTH,
  YEAROFBIRTH,
  PLACEOFBIRTH,
  CITYOFBIRTH,
  ADDRESS,
  PLZ,
  CITY,
  SUBURB,
  STATE,
  STREET,
  COUNTRY,
  AREA,
  SEX,
  SEX_CODE,
  INSURANCENUMBER,
  REGISTRATION_DATE,
  STREET_NUMBER_PREFIX,
  STREET_NUMBER,
  STREET_NUMBER_SUFFIX,
  DIRECTION_PREFIX,
  STREET_NAME,
  STREET_TYPE,
  DIRECTION_SUFFIX,
  EXTENSION,
  MAILING_ADDRESS_LINE_ONE,
  MAILING_ADDRESS_LINE_TWO,
  MAILING_ADDRESS_LINE_THREE,
  MAILING_ADDRESS_LINE_FOUR,
  MAILING_ADDRESS_LINE_FIVE,
  COUNTY_CODE,
  COUNTY_NAME,
  JURISDICTION_CODE,
  JURISDICTION_NAME,
  PHONE_NUMBER,
  RACE_CODE,
  RACE_DESC,
  ETHIC_CODE,
  ETHIC_DESC,
  PARTY,
  PARTY_DESC;

  private static final Map<PersonalAttributeType, String> SHORT_NAMES = new EnumMap<>(PersonalAttributeType.class);

  static {
    // Short names (abbreviations) — entries for every enum constant
    SHORT_NAMES.put(FIRSTNAME, "FN");
    SHORT_NAMES.put(MIDDLENAME, "MN");
    SHORT_NAMES.put(LASTNAME, "LN");
    SHORT_NAMES.put(NAME_SUFFIX, "NS");
    SHORT_NAMES.put(NAMEATBIRTH, "NAB");
    SHORT_NAMES.put(DATEOFBIRTH, "DOB");
    SHORT_NAMES.put(DAYOFBIRTH, "DOB_DAY");
    SHORT_NAMES.put(MONTHOFBIRTH, "MOB");
    SHORT_NAMES.put(YEAROFBIRTH, "YOB");
    SHORT_NAMES.put(PLACEOFBIRTH, "POB");
    SHORT_NAMES.put(ADDRESS, "ADDR");
    SHORT_NAMES.put(PLZ, "PLZ");
    SHORT_NAMES.put(CITY, "CITY");
    SHORT_NAMES.put(SUBURB, "SUB");
    SHORT_NAMES.put(STATE, "STA");
    SHORT_NAMES.put(STREET, "STR");
    SHORT_NAMES.put(COUNTRY, "CO");
    SHORT_NAMES.put(AREA, "AREA");
    SHORT_NAMES.put(SEX, "S");
    SHORT_NAMES.put(SEX_CODE, "SC");
    SHORT_NAMES.put(INSURANCENUMBER, "IN");
    SHORT_NAMES.put(REGISTRATION_DATE, "REG"); // avoid RD collision
    SHORT_NAMES.put(STREET_NUMBER_PREFIX, "SNP");
    SHORT_NAMES.put(STREET_NUMBER, "SN");
    SHORT_NAMES.put(STREET_NUMBER_SUFFIX, "SNS");
    SHORT_NAMES.put(DIRECTION_PREFIX, "DP");
    SHORT_NAMES.put(STREET_NAME, "SNAME");
    SHORT_NAMES.put(STREET_TYPE, "STYPE");
    SHORT_NAMES.put(DIRECTION_SUFFIX, "DS");
    SHORT_NAMES.put(EXTENSION, "EXT");
    SHORT_NAMES.put(MAILING_ADDRESS_LINE_ONE, "MAL1");
    SHORT_NAMES.put(MAILING_ADDRESS_LINE_TWO, "MAL2");
    SHORT_NAMES.put(MAILING_ADDRESS_LINE_THREE, "MAL3");
    SHORT_NAMES.put(MAILING_ADDRESS_LINE_FOUR, "MAL4");
    SHORT_NAMES.put(MAILING_ADDRESS_LINE_FIVE, "MAL5");
    SHORT_NAMES.put(COUNTY_CODE, "CC");
    SHORT_NAMES.put(COUNTY_NAME, "CN");
    SHORT_NAMES.put(JURISDICTION_CODE, "JC");
    SHORT_NAMES.put(JURISDICTION_NAME, "JN");
    SHORT_NAMES.put(PHONE_NUMBER, "PN");
    SHORT_NAMES.put(RACE_CODE, "RC");
    SHORT_NAMES.put(RACE_DESC, "RACED");
    SHORT_NAMES.put(ETHIC_CODE, "EC");
    SHORT_NAMES.put(ETHIC_DESC, "ETHD");
    SHORT_NAMES.put(PARTY, "PTY");
    SHORT_NAMES.put(PARTY_DESC, "PTYD");
  }

  // Instance helpers
  public String asString() {
    return this.name();
  }

  public String getShortName() {
    return SHORT_NAMES.get(this);
  }

  public static String getShortName(PersonalAttributeType t) {
    return (t == null) ? null : SHORT_NAMES.get(t);
  }

  /**
   * Try to parse a PersonalAttributeType from a string name. Returns Optional.empty() if parsing fails.
   */
  public static Optional<PersonalAttributeType> fromStringSafe(String name) {
    if (name == null || name.isEmpty()) return Optional.empty();
    try {
      return Optional.of(PersonalAttributeType.valueOf(name));
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  // Comparator for String ids: try to parse into PersonalAttributeType and compare by ordinal; fall back to string compare
  public static class AttributeNameComparator implements Comparator<String> {
    private final AttributeTypeComparator comparator = new AttributeTypeComparator();

    @Override
    public int compare(String id0, String id1) {
      if (Objects.equals(id0, id1)) return 0;
      if (id0 == null) return -1;
      if (id1 == null) return 1;

      Optional<PersonalAttributeType> a0 = PersonalAttributeType.fromStringSafe(id0);
      Optional<PersonalAttributeType> a1 = PersonalAttributeType.fromStringSafe(id1);

      if (a0.isPresent() && a1.isPresent()) {
        return comparator.compare(a0.get(), a1.get());
      } else if (a0.isPresent()) {
        // parsed one sorts before unknown
        return -1;
      } else if (a1.isPresent()) {
        return 1;
      } else {
        return id0.compareTo(id1);
      }
    }
  }

  // Comparator for PersonalAttributeType (by ordinal / natural enum ordering)
  public static class AttributeTypeComparator implements Comparator<PersonalAttributeType> {
    @Override
    public int compare(PersonalAttributeType o1, PersonalAttributeType o2) {
      if (o1 == o2) return 0;
      if (o1 == null) return -1;
      if (o2 == null) return 1;
      return Integer.compare(o1.ordinal(), o2.ordinal());
    }
  }
}

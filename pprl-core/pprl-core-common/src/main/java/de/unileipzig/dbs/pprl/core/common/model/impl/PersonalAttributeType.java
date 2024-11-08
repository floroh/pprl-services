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

import java.util.Comparator;
import java.util.Map;

public enum PersonalAttributeType {
  FIRSTNAME, MIDDLENAME, LASTNAME, GENDER, NAMEATBIRTH, DATEOFBIRTH, DAYOFBIRTH, MONTHOFBIRTH, YEAROFBIRTH,
  PLACEOFBIRTH, ADDRESS, PLZ, CITY, SUBURB, STATE, STREET, COUNTRY, INSURANCENUMBER, REGISTRATION_DATE;

  private static final Map<PersonalAttributeType, String> displayNames = Map.ofEntries(
    Map.entry(FIRSTNAME, "Vorname"),
    Map.entry(MIDDLENAME, "Weitere Vornamen"),
    Map.entry(LASTNAME, "Nachname"),
    Map.entry(GENDER, "Geschlecht"),
    Map.entry(NAMEATBIRTH, "Geburtsname"),
    Map.entry(DATEOFBIRTH, "Geburtsdatum"),
    Map.entry(DAYOFBIRTH, "Geburtstag"),
    Map.entry(MONTHOFBIRTH, "Geburtsmonat"),
    Map.entry(YEAROFBIRTH, "Geburtsjahr"),
    Map.entry(PLACEOFBIRTH, "Geburtsort"),
    Map.entry(ADDRESS, "Adresse"),
    Map.entry(PLZ, "Postleitzahl"),
    Map.entry(CITY, "Stadt"),
    Map.entry(SUBURB, "Vorort"),
    Map.entry(STATE, "Bundesland"),
    Map.entry(STREET, "Straße"),
    Map.entry(COUNTRY, "Land"),
    Map.entry(INSURANCENUMBER, "Versicherungsnummer"),
    Map.entry(REGISTRATION_DATE, "Registrierungsdatum")
  );

  private static final Map<PersonalAttributeType, String> shortNames = Map.ofEntries(
    Map.entry(FIRSTNAME, "FN"),
    Map.entry(MIDDLENAME, "MN"),
    Map.entry(LASTNAME, "LN"),
    Map.entry(GENDER, "G"),
    Map.entry(NAMEATBIRTH, "NAB"),
    Map.entry(DATEOFBIRTH, "DOB"),
    Map.entry(DAYOFBIRTH, "dOB"),
    Map.entry(MONTHOFBIRTH, "MOB"),
    Map.entry(YEAROFBIRTH, "YOB"),
    Map.entry(PLACEOFBIRTH, "POB"),
    Map.entry(ADDRESS, "ADDR"),
    Map.entry(PLZ, "PLZ"),
    Map.entry(CITY, "CI"),
    Map.entry(SUBURB, "SU"),
    Map.entry(STATE, "STA"),
    Map.entry(STREET, "STR"),
    Map.entry(COUNTRY, "CO"),
    Map.entry(INSURANCENUMBER, "IN"),
    Map.entry(REGISTRATION_DATE, "RD")
  );

  public String asString() {
    return this.name();
  }

  public static String getDisplayName(PersonalAttributeType type) {
    return displayNames.get(type);
  }

  public static String getShortName(PersonalAttributeType type) {
    return shortNames.get(type);
  }

  public String getShortName() {
    return shortNames.get(this);
  }

  public static class AttributeNameComparator implements Comparator<String> {
    private PersonalAttributeTypeComparator comparator = new PersonalAttributeTypeComparator();

    public int compare(String id0, String id1) {
      try {
        PersonalAttributeType pat0 = PersonalAttributeType.valueOf(id0);
        PersonalAttributeType pat1 = PersonalAttributeType.valueOf(id1);
        return comparator.compare(pat0, pat1);
      } catch (IllegalArgumentException e) {
        return id0.compareTo(id1);
      }
    }
  }

  public static class PersonalAttributeTypeComparator implements Comparator<PersonalAttributeType> {
    public int compare(PersonalAttributeType id0, PersonalAttributeType id1) {
      return id0.compareTo(id1);
    }
  }
}

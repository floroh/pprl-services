package de.unileipzig.dbs.pprl.service.generator.selection.model.ncvr.panse;

import de.unileipzig.dbs.pprl.core.common.model.impl.PersonalAttributeType;
import de.unileipzig.dbs.pprl.service.generator.selection.model.common.GenericRawRecord;

import java.util.LinkedHashMap;
import java.util.Map;

public class NcvrPanseConverter {

  /**
   * Convert an NcvrRecord to a GenericRawRecord.
   *
   * @param src      source record
   * @return GenericRawRecord with voterId and attributes map populated
   */
  public static GenericRawRecord toGeneric(NcvrRecord src) {
    Map<String, String> attrs = new LinkedHashMap<>(); // preserve insertion order (helpful for debugging)

    putIfNotNull(attrs, PersonalAttributeType.FIRSTNAME,       normalize(src.getFirstName()));
    putIfNotNull(attrs, PersonalAttributeType.MIDDLENAME,      normalize(src.getMidlName()));
    putIfNotNull(attrs, PersonalAttributeType.LASTNAME,        normalize(src.getLastName()));
    putIfNotNull(attrs, PersonalAttributeType.YEAROFBIRTH,     normalize(src.getYob()));
    putIfNotNull(attrs, PersonalAttributeType.PLACEOFBIRTH,    normalize(src.getBirthPlace()));
    putIfNotNull(attrs, PersonalAttributeType.PLZ,             normalize(src.getZipCode()));
    putIfNotNull(attrs, PersonalAttributeType.CITY,            normalize(src.getResCityDesc()));
    putIfNotNull(attrs, PersonalAttributeType.STATE,           normalize(src.getStateCd()));
//    putIfNotNull(attrs, PersonalAttributeType.AREA, normalize(src.getAreaCd()));
    putIfNotNull(attrs, PersonalAttributeType.SEX,             normalize(src.getSex()));
    putIfNotNull(attrs, PersonalAttributeType.SEX_CODE,        normalize(src.getSexCode()));
    putIfNotNull(attrs, PersonalAttributeType.STREET_NUMBER,   normalize(src.getHouseNum()));
    putIfNotNull(attrs, PersonalAttributeType.STREET_NAME,     normalize(src.getStreetName()));
    putIfNotNull(attrs, PersonalAttributeType.STREET_TYPE,     normalize(src.getStreetTypeCd()));
    putIfNotNull(attrs, PersonalAttributeType.EXTENSION,       normalize(src.getUnitNum()));
//    putIfNotNull(attrs, PersonalAttributeType.COUNTY_CODE,     normalize(src.getCountyId()));
    putIfNotNull(attrs, PersonalAttributeType.COUNTY_NAME,     normalize(src.getCountyDesc()));
    putIfNotNull(attrs, PersonalAttributeType.PHONE_NUMBER,    normalize(src.getPhoneNum()));
    putIfNotNull(attrs, PersonalAttributeType.RACE_CODE,       normalize(src.getRaceCode()));
    putIfNotNull(attrs, PersonalAttributeType.RACE_DESC,       normalize(src.getRaceDesc()));
    putIfNotNull(attrs, PersonalAttributeType.ETHIC_CODE,      normalize(src.getEthnicCode()));
    putIfNotNull(attrs, PersonalAttributeType.ETHIC_DESC,      normalize(src.getEthnicDesc()));
    putIfNotNull(attrs, PersonalAttributeType.PARTY,           normalize(src.getPartyCd()));
    putIfNotNull(attrs, PersonalAttributeType.PARTY_DESC,      normalize(src.getPartyDesc()));

    // Build GenericRawRecord: set voterId from NcvrRecord.id (use src.getId() directly)
    return GenericRawRecord.builder()
            .voterId(normalize(src.getId()))   // you might prefer src.getId() without normalize
            .attributes(attrs)                 // Lombok @Singular provides attributes(map) setter
            .build();
  }

  // --------------------- helpers ---------------------

  private static void putIfNotNull(Map<String, String> m, PersonalAttributeType type, String value) {
    // store only keys with non-null values; comment this out if you want explicit empty-string values
    if (value != null) {
      m.put(type.name(), value);
    }
  }

  private static String normalize(String s) {
    if (s == null) return null;
    String t = s.trim();
    return t.isEmpty() ? null : t;
  }

}
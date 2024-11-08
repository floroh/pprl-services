package de.unileipzig.dbs.pprl.core.common.monitoring;

import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;

import java.util.Objects;

public class Tag {

  private String id0;

  private String id1;

  private String attribute;

  private String tag;

  private String stringValue;

  private Double numericValue;

  public Tag(String id0, String id1, String attribute, String tag, String stringValue, Double numericValue) {
    this.id0 = id0;
    this.id1 = id1;
    this.attribute = attribute;
    this.tag = tag;
    this.stringValue = stringValue;
    this.numericValue = numericValue;
  }

  private Tag() {
  }

  public static Tag create(Record record, String attribute, String tag, String stringValue,
    Double numericValue) {
    return new Tag(record.getId().getUniqueId(), null,
      attribute,
      tag,
      stringValue,
      numericValue
    );
  }

  public static Tag create(Record record, String attribute, String tag) {
    return new Tag(record.getId().getUniqueId(), null,
      attribute,
      tag,
      null,
      null
    );
  }

  public static Tag create(String tag) {
    return new Tag(null, null, null, tag, null, null);
  }

  public static Tag create(String tag, String stringValue, Double numericValue) {
    return new Tag(null, null, null, tag, stringValue, numericValue);
  }

  public static Tag create(String attribute, String tag, String stringValue, Double numericValue) {
    return new Tag(null, null, attribute, tag, stringValue, numericValue);
  }

  public static Tag create(RecordPair pair, String attribute, String tag, String stringValue,
    Double numericValue) {
    return new Tag(pair.getLeftRecord().getId().getUniqueId(), pair.getRightRecord().getId().getUniqueId(),
      attribute,
      tag,
      stringValue,
      numericValue
    );
  }

  public static Tag create(RecordPair pair, String attribute, String tag) {
    return new Tag(pair.getLeftRecord().getId().getUniqueId(), pair.getRightRecord().getId().getUniqueId(),
      attribute,
      tag,
      null,
      null
    );
  }

  public static Tag addIDs(Tag tag, RecordPair pair) {
    tag.setId0(pair.getLeftRecord().getId().getUniqueId());
    tag.setId1(pair.getRightRecord().getId().getUniqueId());
    return tag;
  }

  public Tag addID(Record record) {
    return addID(this, record);
  }

  public static Tag addID(Tag tag, Record record) {
    tag.setId0(record.getId().getUniqueId());
    return tag;
  }


  public String getId0() {
    return id0;
  }

  public void setId0(String id0) {
    this.id0 = id0;
  }

  public String getId1() {
    return id1;
  }

  public void setId1(String id1) {
    this.id1 = id1;
  }

  public String getAttribute() {
    return attribute;
  }

  public void setAttribute(String attribute) {
    this.attribute = attribute;
  }


  public String getTag() {
    return tag;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }

  public String getStringValue() {
    return stringValue;
  }

  public void setStringValue(String stringValue) {
    this.stringValue = stringValue;
  }

  public Double getNumericValue() {
    return numericValue;
  }

  public void setNumericValue(Double numericValue) {
    this.numericValue = numericValue;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Tag tag1 = (Tag) o;

    if (!Objects.equals(id0, tag1.id0)) {
      return false;
    }
    if (!Objects.equals(id1, tag1.id1)) {
      return false;
    }
    if (!Objects.equals(attribute, tag1.attribute)) {
      return false;
    }
    return tag.equals(tag1.tag);
  }

  @Override
  public int hashCode() {
    int result = id0 != null ? id0.hashCode() : 0;
    result = 31 * result + (id1 != null ? id1.hashCode() : 0);
    result = 31 * result + (attribute != null ? attribute.hashCode() : 0);
    result = 31 * result + tag.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "Tag{" +
      "id0='" + id0 + '\'' +
      ", id1='" + id1 + '\'' +
      ", attribute='" + attribute + '\'' +
      ", tag='" + tag + '\'' +
      ", stringValue='" + stringValue + '\'' +
      ", numericValue=" + numericValue +
      '}';
  }
}

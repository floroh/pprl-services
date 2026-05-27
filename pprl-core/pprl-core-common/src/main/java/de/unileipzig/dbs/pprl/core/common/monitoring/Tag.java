package de.unileipzig.dbs.pprl.core.common.monitoring;

import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;

import java.util.Objects;

public class Tag {

  public static final String TYPE_STRUCTURE = "s";
  public static final String TYPE_PLAIN = "pt";
  public static final String TYPE_ENCODED = "enc";
  public static final String ORIGIN_DATA_OWNER = "do";
  public static final String ORIGIN_LINKAGE_UNIT = "lu";
  public static final String ORIGIN_DATA_CORRUPTER = "c";
  public static final String ORIGIN_DATA_GENERATOR = "g";

  private String id0;

  private String id1;

  private String attribute;

  private String tag;

  private String stringValue;

  private Double numericValue;

  /**
   * Representation of the underlying record: pt (plaintext), bf (bloomfilter)
   */
  private String type;

  /**
   * Origin of the tag, e.g. g (generator), c (corrupter), do (data-owner), lu (linkage-unit)
   */
  private String origin;

  public Tag(String id0, String id1, String attribute, String tag, String stringValue, Double numericValue, String type, String origin) {
    this.id0 = id0;
    this.id1 = id1;
    this.attribute = attribute;
    this.tag = tag;
    this.stringValue = stringValue;
    this.numericValue = numericValue;
    this.type = type;
    this.origin = origin;
  }

  public Tag(String id0, String id1, String attribute, String tag, String stringValue, Double numericValue) {
    this(id0, id1, attribute, tag, stringValue, numericValue, null, null);
  }

  private Tag() {
  }

  public static Tag create(Record record, String attribute, String tag, String stringValue,
    Double numericValue) {
    return new Tag(record.getId().getUniqueLikeId(), null,
      attribute,
      tag,
      stringValue,
      numericValue,
      null,
      null
    );
  }

  public static Tag create(Record record, String attribute, String tag) {
    return new Tag(record.getId().getUniqueLikeId(), null,
      attribute,
      tag,
      null,
      null,
      null,
      null
    );
  }

  public static Tag create(String tag) {
    return new Tag(null, null, null, tag, null, null, null, null);
  }

  public static Tag create(String tag, String stringValue, Double numericValue) {
    return new Tag(null, null, null, tag, stringValue, numericValue, null, null);
  }

  public static Tag create(String attribute, String tag, String stringValue, Double numericValue) {
    return new Tag(null, null, attribute, tag, stringValue, numericValue, null, null);
  }

  public static Tag create(RecordPair pair, String attribute, String tag, String stringValue,
    Double numericValue) {
    return new Tag(pair.getLeftRecord().getId().getUniqueLikeId(), pair.getRightRecord().getId().getUniqueLikeId(),
      attribute,
      tag,
      stringValue,
      numericValue,
      null,
      null
    );
  }

  public static Tag create(RecordPair pair, String attribute, String tag) {
    return new Tag(pair.getLeftRecord().getId().getUniqueLikeId(), pair.getRightRecord().getId().getUniqueLikeId(),
      attribute,
      tag,
      null,
      null,
      null,
      null
    );
  }

  public static Tag addIDs(Tag tag, RecordPair pair) {
    tag.setId0(pair.getLeftRecord().getId().getUniqueLikeId());
    tag.setId1(pair.getRightRecord().getId().getUniqueLikeId());
    return tag;
  }

  public Tag addID(Record record) {
    return addID(this, record);
  }

  public static Tag addID(Tag tag, Record record) {
    tag.setId0(record.getId().getUniqueLikeId());
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

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getOrigin() {
    return origin;
  }

  public void setOrigin(String origin) {
    this.origin = origin;
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
    if (!Objects.equals(type, tag1.type)) {
      return false;
    }
    if (!Objects.equals(origin, tag1.origin)) {
      return false;
    }
    return tag.equals(tag1.tag);
  }

  @Override
  public int hashCode() {
    int result = id0 != null ? id0.hashCode() : 0;
    result = 31 * result + (id1 != null ? id1.hashCode() : 0);
    result = 31 * result + (attribute != null ? attribute.hashCode() : 0);
    result = 31 * result + (type != null ? type.hashCode() : 0);
    result = 31 * result + (origin != null ? origin.hashCode() : 0);
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
      ", numericValue=" + numericValue + '\'' +
      ", type=" + type + '\'' +
      ", origin=" + origin +
      '}';
  }
}

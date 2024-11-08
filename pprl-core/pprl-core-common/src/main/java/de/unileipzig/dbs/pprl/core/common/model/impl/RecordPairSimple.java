package de.unileipzig.dbs.pprl.core.common.model.impl;

import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;

import java.util.HashMap;
import java.util.Objects;

public class RecordPairSimple extends AbstractRecordPair {

  private Record leftRecord;

  private Record rightRecord;

  public RecordPairSimple(Record leftRecord, Record rightRecord) {
    super();
    this.leftRecord = leftRecord;
    this.rightRecord = rightRecord;
  }

  public RecordPairSimple(Record leftRecord, Record rightRecord, double similarity) {
    super();
    this.similarity = similarity;
    this.leftRecord = leftRecord;
    this.rightRecord = rightRecord;
  }

  public RecordPairSimple(Record leftRecord, Record rightRecord, double similarity,
    MatchGrade classification) {
    super(similarity, classification);
    this.leftRecord = leftRecord;
    this.rightRecord = rightRecord;
  }

  @Override
  public Record getLeftRecord() {
    return leftRecord;
  }

  @Override
  public RecordPair setLeftRecord(Record record) {
    leftRecord = record;
    return this;
  }

  @Override
  public Record getRightRecord() {
    return rightRecord;
  }

  @Override
  public RecordPair setRightRecord(Record record) {
    rightRecord = record;
    return this;
  }

  @Override
  public RecordPair duplicate() {
    RecordPairSimple duplicate =
      new RecordPairSimple(leftRecord.duplicate(), rightRecord.duplicate(), similarity, classification);
    this.getTags().forEach(duplicate::addTag);
    this.getAttributeSimilarities().ifPresent(as -> duplicate.setAttributeSimilarities(new HashMap<>(as)));
    return duplicate;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    RecordPairSimple that = (RecordPairSimple) o;

    if (Double.compare(that.similarity, similarity) != 0) {
      return false;
    }
    if (!leftRecord.getId().equals(that.leftRecord.getId())) {
      if (leftRecord.getId().equals(that.rightRecord.getId()) &&
        (rightRecord.getId().equals(that.leftRecord.getId()))) {
        return true;
      }
      return false;
    }
    if (!rightRecord.getId().equals(that.rightRecord.getId())) {
      if (rightRecord.getId().equals(that.leftRecord.getId()) &&
        (leftRecord.getId().equals(that.rightRecord.getId()))) {
        return true;
      }
      return false;
    }
    if (classification != that.classification) {
      return false;
    }
    return Objects.equals(attributeSimilarities, that.attributeSimilarities);
  }

  @Override
  public int hashCode() {
    int prime = 31;
    int result = prime * leftRecord.getId().getUniqueId().hashCode();
    result = result + prime * rightRecord.getId().getUniqueId().hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "RecordPairSimple{" +
      "leftRecord=" + leftRecord +
      ", rightRecord=" + rightRecord +
      ", similarity=" + similarity +
      ", classification=" + classification +
      ", attributeSimilarities=" + attributeSimilarities +
      '}';
  }

}

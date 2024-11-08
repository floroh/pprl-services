package de.unileipzig.dbs.pprl.core.common.model.impl;

import de.unileipzig.dbs.pprl.core.common.model.api.RecordId;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordIdPair;

public class RecordIdPairSimple implements RecordIdPair {

  private RecordId leftRecordId;

  private RecordId rightRecordId;

  public RecordIdPairSimple(RecordId leftRecordId, RecordId rightRecordId) {
    this.leftRecordId = leftRecordId;
    this.rightRecordId = rightRecordId;
  }

  @Override
  public RecordId getLeftRecordId() {
    return leftRecordId;
  }

  @Override
  public RecordId getRightRecordId() {
    return rightRecordId;
  }

  public RecordIdPair duplicate() {
    return new RecordIdPairSimple(leftRecordId.duplicate(), rightRecordId.duplicate());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    RecordIdPairSimple that = (RecordIdPairSimple) o;

    if (leftRecordId.equals(that.leftRecordId) && rightRecordId.equals(that.rightRecordId)) {
      return true;
    } else if (leftRecordId.equals(that.rightRecordId) && rightRecordId.equals(that.leftRecordId)) {
      return true;
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = leftRecordId.hashCode();
    result = result + rightRecordId.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "RecordIdPairSimple{" +
      "leftRecordId=" + leftRecordId +
      ", rightRecordId=" + rightRecordId +
      '}';
  }
}

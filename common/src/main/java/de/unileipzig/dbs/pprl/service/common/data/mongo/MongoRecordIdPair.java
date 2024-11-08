package de.unileipzig.dbs.pprl.service.common.data.mongo;

import de.unileipzig.dbs.pprl.core.common.model.api.RecordId;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordIdPair;
import de.unileipzig.dbs.pprl.core.matcher.classification.Classifier;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class MongoRecordIdPair implements RecordIdPair {

  private RecordId leftRecordId;

  private RecordId rightRecordId;

  private Classifier.Label label;

  public MongoRecordIdPair(RecordId leftRecordId, RecordId rightRecordId, Classifier.Label label) {
    this.leftRecordId = leftRecordId;
    this.rightRecordId = rightRecordId;
    this.label = label;
  }

  public MongoRecordIdPair(RecordId leftRecordId, RecordId rightRecordId) {
    this(leftRecordId, rightRecordId, Classifier.Label.TRUE_MATCH);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    MongoRecordIdPair that = (MongoRecordIdPair) o;

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
}

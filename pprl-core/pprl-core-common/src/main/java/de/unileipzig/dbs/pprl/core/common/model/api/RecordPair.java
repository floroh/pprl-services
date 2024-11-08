package de.unileipzig.dbs.pprl.core.common.model.api;

import de.unileipzig.dbs.pprl.core.common.comparators.RecordIdComparator;
import de.unileipzig.dbs.pprl.core.common.model.impl.MatchGrade;
import de.unileipzig.dbs.pprl.core.common.monitoring.Tag;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface RecordPair extends RecordIdPair {

  Record getLeftRecord();

  RecordPair setLeftRecord(Record record);

  Record getRightRecord();

  RecordPair setRightRecord(Record record);

  double getSimilarity();

  RecordPair setSimilarity(double similarity);

  MatchGrade getClassification();

  RecordPair setClassification(MatchGrade classification);

  Optional<Map<String, Double>> getAttributeSimilarities();

  RecordPair setAttributeSimilarities(Map<String, Double> attributeSimilarities);

  RecordPair addTag(Tag tag);

  RecordPair removeTag(Tag tag);

  default RecordPair addTag(String tag) {
    return addTag(Tag.create(tag));
  }

  default RecordPair removeTag(String tag) {
    return removeTag(Tag.create(tag));
  }

  Collection<Tag> getTags();

  RecordPair duplicate();

  default Collection<Record> getRecords() {
    return List.of(getLeftRecord(), getRightRecord());
  }

  default Record getRecord(Side side) {
    switch (side) {
      case LEFT:
        return getLeftRecord();
      case RIGHT:
        return getRightRecord();
    }
    throw new RuntimeException();
  }

  static <T extends RecordPair> T sortRecords(T recordPair) {
    Record leftRecord = recordPair.getLeftRecord();
    Record rightRecord = recordPair.getRightRecord();
    if (new RecordIdComparator()
      .compare(leftRecord.getId(), rightRecord.getId()) > 0) {
      recordPair.setLeftRecord(rightRecord);
      recordPair.setRightRecord(leftRecord);
    }
    return recordPair;
  }

  static <T extends RecordPair> T sortRecordsBySource(
    T recordPair) {
    Record leftRecord = recordPair.getLeftRecord();
    Record rightRecord = recordPair.getRightRecord();
    if (new RecordIdComparator(true)
      .compare(leftRecord.getId(), rightRecord.getId()) > 0) {
      recordPair.setLeftRecord(rightRecord);
      recordPair.setRightRecord(leftRecord);
    }
    return recordPair;
  }

  enum Side {
    LEFT, RIGHT;

    public static Side other(Side side) {
      switch (side) {
        case LEFT:
          return RIGHT;

        case RIGHT:
          return LEFT;

        default:
          return null;
      }
    }
  }
}

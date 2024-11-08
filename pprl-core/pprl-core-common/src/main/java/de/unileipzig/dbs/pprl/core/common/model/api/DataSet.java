package de.unileipzig.dbs.pprl.core.common.model.api;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

public interface DataSet {

  void addRecord(Record record);

  default void addRecords(Collection<Record> records) {
    records.forEach(this::addRecord);
  }

  Optional<Record> getRecord(RecordId id);

  Collection<Record> getAllRecords();

  Collection<Record> getRecordsBySource(String sourceName);

  default long size() {
    return getAllRecords().size();
  }

  default Iterator<Record> getRecordIterator() {
    return getAllRecords().iterator();
  }
}

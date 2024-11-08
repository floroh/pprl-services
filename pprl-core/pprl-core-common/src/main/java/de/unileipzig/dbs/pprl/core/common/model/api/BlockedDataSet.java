package de.unileipzig.dbs.pprl.core.common.model.api;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public interface BlockedDataSet extends DataSet {

  Collection<Record> getRecordsByBlockingKey(BlockingKey bk);

  default Collection<Record> getRecordsByBlockingKeys(Collection<BlockingKey> bks) {
    if (bks.isEmpty()) {
      return getAllRecords();
    }

    Set<Record> records = new HashSet<>();
    for (BlockingKey bk : bks) {
      records.addAll(getRecordsByBlockingKey(bk));
    }
    return records;
  }

  void addBlockedRecord(Record record, Collection<BlockingKey> blockingKeys);
}

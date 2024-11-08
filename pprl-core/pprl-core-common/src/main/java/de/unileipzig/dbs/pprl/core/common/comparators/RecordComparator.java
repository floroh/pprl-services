package de.unileipzig.dbs.pprl.core.common.comparators;

import de.unileipzig.dbs.pprl.core.common.model.api.Record;

import java.util.Comparator;

public class RecordComparator implements Comparator<Record> {
  private RecordIdComparator comparator = new RecordIdComparator();

  public int compare(Record id0, Record id1) {
    return comparator.compare(id0.getId(), id1.getId());
  }
}

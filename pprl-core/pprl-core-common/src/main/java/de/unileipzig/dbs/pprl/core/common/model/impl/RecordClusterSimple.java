package de.unileipzig.dbs.pprl.core.common.model.impl;

import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordCluster;

import java.util.Collection;
import java.util.HashSet;

public class RecordClusterSimple implements RecordCluster {

  private Collection<Record> records;

  public RecordClusterSimple(Collection<Record> records) {
    this.records = records;
  }

  public RecordClusterSimple() {
    this.records = new HashSet<>();
  }

  public void add(Collection<Record> newRecords) {
    newRecords.forEach(this::addRecord);
  }

  @Override
  public void addRecord(Record newRecord) {
    this.records.add(newRecord);
  }

  public void remove(Record record) {
    this.records.remove(record);
  }

  public Collection<Record> getRecords() {
    return records;
  }

  @Override
  public Record getRepresentative() {
    return records.iterator().next();
  }

  @Override
  public String toString() {
    return "RecordCluster{" + "records=" + records + '}';
  }

}

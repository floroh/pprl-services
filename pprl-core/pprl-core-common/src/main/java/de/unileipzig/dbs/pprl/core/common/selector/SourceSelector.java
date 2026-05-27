package de.unileipzig.dbs.pprl.core.common.selector;

import de.unileipzig.dbs.pprl.core.common.model.api.Record;

public class SourceSelector implements Selector<Record> {

  private String source;

  public SourceSelector(String source) {
    this.source = source;
  }

  @Override
  public boolean test(Record record) {
    return record.getId().getSourceId().equals(source);
  }
}

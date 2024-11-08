package de.unileipzig.dbs.pprl.core.matcher.model.impl;

import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.matcher.model.api.SearchResultEntry;

public class BasicSearchResultEntry implements SearchResultEntry {

  private final Record record;

  private final double similarity;

  public BasicSearchResultEntry(Record record, double similarity) {
    this.record = record;
    this.similarity = similarity;
  }

  @Override
  public Record getRecord() {
    return record;
  }

  @Override
  public double getSimilarity() {
    return similarity;
  }
}

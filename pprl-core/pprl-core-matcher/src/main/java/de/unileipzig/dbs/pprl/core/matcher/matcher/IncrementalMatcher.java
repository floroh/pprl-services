package de.unileipzig.dbs.pprl.core.matcher.matcher;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordId;
import de.unileipzig.dbs.pprl.core.common.model.api.BlockedDataSet;
import de.unileipzig.dbs.pprl.core.matcher.model.api.SearchResult;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property = "@class")
public interface IncrementalMatcher extends Matcher {

  @JsonIgnore
  void setDataSet(BlockedDataSet dataSet);

  SearchResult search(Record query);

  RecordId insert(Record query);
}

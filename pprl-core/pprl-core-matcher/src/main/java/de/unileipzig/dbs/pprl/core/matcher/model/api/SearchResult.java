package de.unileipzig.dbs.pprl.core.matcher.model.api;

import de.unileipzig.dbs.pprl.core.common.model.api.Record;

import java.util.List;
import java.util.Optional;

public interface SearchResult {

  List<SearchResultEntry> getEntries();

  default Optional<Record> bestMatch() {
    return getEntries().stream().findFirst().map(SearchResultEntry::getRecord);
  }

}

package de.unileipzig.dbs.pprl.core.matcher.model.impl;

import de.unileipzig.dbs.pprl.core.matcher.model.api.SearchResult;
import de.unileipzig.dbs.pprl.core.matcher.model.api.SearchResultEntry;

import java.util.ArrayList;
import java.util.List;

public class BasicSearchResult implements SearchResult {

  private List<SearchResultEntry> entries;

  public BasicSearchResult(List<SearchResultEntry> entries) {
    this.entries = entries;
  }

  public BasicSearchResult() {
    entries = new ArrayList<>();
  }

  @Override
  public List<SearchResultEntry> getEntries() {
    return entries;
  }

  public void addEntry(SearchResultEntry entry) {
    entries.add(entry);
  }
}

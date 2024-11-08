package de.unileipzig.dbs.pprl.core.matcher.model.api;

import de.unileipzig.dbs.pprl.core.common.model.api.Record;

public interface SearchResultEntry {

  Record getRecord();

  double getSimilarity();

}

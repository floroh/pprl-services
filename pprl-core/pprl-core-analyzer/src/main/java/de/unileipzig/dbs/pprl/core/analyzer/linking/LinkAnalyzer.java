package de.unileipzig.dbs.pprl.core.analyzer.linking;

import de.unileipzig.dbs.pprl.core.analyzer.Analyzer;
import de.unileipzig.dbs.pprl.core.analyzer.results.ResultSet;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;

import java.util.Collection;

public abstract class LinkAnalyzer extends Analyzer {

  public abstract ResultSet analyze(Collection<RecordPair> pairs);

}

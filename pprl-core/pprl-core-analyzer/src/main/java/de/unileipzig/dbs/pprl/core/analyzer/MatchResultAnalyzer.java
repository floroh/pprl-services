package de.unileipzig.dbs.pprl.core.analyzer;

import de.unileipzig.dbs.pprl.core.analyzer.linking.LinkAnalyzer;
import de.unileipzig.dbs.pprl.core.analyzer.results.ResultSet;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatchResultAnalyzer {

  public static final String RESULTS_LINKS = "Links";
  private List<LinkAnalyzer> linkAnalyzer;

  private static Logger logger = LogManager.getLogger(MatchResultAnalyzer.class);

  public MatchResultAnalyzer() {
    linkAnalyzer = new ArrayList<>();
  }

  public AnalysisResult run(Collection<RecordPair> recordPairs) {
    final Map<String, List<ResultSet>> results = new HashMap<>();
    results.put(RESULTS_LINKS, runLinkAnalyzer(recordPairs));
    return new AnalysisResult(results);
  }

  private List<ResultSet> runLinkAnalyzer(Collection<RecordPair> recordPairs) {
    logger.info("Running link analyzers");
    List<ResultSet> results = new ArrayList<>();
    for (LinkAnalyzer analyzer : linkAnalyzer) {
      logger.info("Running analyzer: " + analyzer.getName());
      results.add(analyzer.analyze(recordPairs));
    }
    return results;
  }

  public void addAnalyzer(LinkAnalyzer analyzer) {
    linkAnalyzer.add(analyzer);
  }
}

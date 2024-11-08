/*
 * Copyright © 2018 - 2021 Leipzig University (Database Research Group)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.unileipzig.dbs.pprl.core.analyzer;

import de.unileipzig.dbs.pprl.core.analyzer.attribute.AttributeAnalyzer;
import de.unileipzig.dbs.pprl.core.analyzer.cluster.ClusterAnalyzer;
import de.unileipzig.dbs.pprl.core.analyzer.record.RecordAnalyzer;
import de.unileipzig.dbs.pprl.core.analyzer.results.ResultSet;
import de.unileipzig.dbs.pprl.core.common.RecordUtils;
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordId;
import de.unileipzig.dbs.pprl.core.common.preprocessing.RecordPreprocessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DataSetAnalyzer {
  public static final String RECORD_GROUP_ALL = "all";

  private static final boolean DEFAULT_RUN_PER_SOURCE = true;

  private Map<String, List<ResultSet>> results;
  private boolean runPerSource;

  private RecordPreprocessor recordPreprocessor;
  private List<RecordAnalyzer> recordAnalyzers;
  private List<ClusterAnalyzer> clusterAnalyzers;
  private List<AttributeAnalyzer> attributeAnalyzers;

  private static Logger logger = LogManager.getLogger(DataSetAnalyzer.class);

  public DataSetAnalyzer() {
    this.results = new HashMap<>();
    this.runPerSource = DEFAULT_RUN_PER_SOURCE;
    recordAnalyzers = new ArrayList<>();
    clusterAnalyzers = new ArrayList<>();
    attributeAnalyzers = new ArrayList<>();
  }

  public AnalysisResult run(List<Record> records) {
    results.clear();
    if (records.isEmpty()) {
      logger.info("Abort Analyzer because the list of input records is empty");
      return new AnalysisResult();
    }

    if (recordPreprocessor != null) {
      records = records.parallelStream().map(recordPreprocessor::preprocess).collect(Collectors.toList());
    }

    final List<ResultSet> curResults = new ArrayList<>();
    if (!recordAnalyzers.isEmpty()) {
      curResults.addAll(runRecordAnalyzers(records));
    }
    if (!clusterAnalyzers.isEmpty()) {
      curResults.addAll(runClusterAnalyzers(records));
    }
    if (!attributeAnalyzers.isEmpty()) {
      curResults.addAll(runAttributeAnalyzers(records));
    }
    results.put(RECORD_GROUP_ALL, curResults);

    if (runPerSource && RecordUtils.numberOfSources(records) > 1) {
      logger.info("Running analyzers for each source");
      for (Map.Entry<String, List<Record>> group : RecordUtils.groupById(records, RecordId.SOURCE_ID)
        .entrySet()) {
        logger.info("Running analyzers for source: " + group.getKey());
        results.put(group.getKey(), runAttributeAnalyzers(group.getValue()));
      }
    }
    return new AnalysisResult(results);
  }

  public List<ResultSet> getResults(String group) {
    return results.get(group);
  }

  private List<ResultSet> runRecordAnalyzers(List<Record> records) {
    logger.info("Running record analyzers");
    List<ResultSet> results = new ArrayList<>();
    for (RecordAnalyzer recordAnalyzer : recordAnalyzers) {
      logger.info("Running analyzer: " + recordAnalyzer.getName());
      results.add(recordAnalyzer.analyze(records));
    }
    return results;
  }

  private List<ResultSet> runClusterAnalyzers(List<Record> records) {
    logger.info("Running cluster analyzers");
    List<ResultSet> results = new ArrayList<>();
    Map<String, List<Record>> clusters = ClusterAnalyzer.prepareRecords(records);
    if (clusters.size() == records.size()) {
      logger.info("Skipping cluster analyzers because all clusters are singletons");
    } else {
      for (ClusterAnalyzer clusterAnalyzer : clusterAnalyzers) {
        logger.info("Running analyzer: " + clusterAnalyzer.getName());
        results.add(clusterAnalyzer.analyze(clusters));
      }
    }
    return results;
  }

  private List<ResultSet> runAttributeAnalyzers(List<Record> records) {
    return runAttributeAnalyzers(records, attributeAnalyzers);
  }
  public static List<ResultSet> runAttributeAnalyzers(List<Record> records,
    List<AttributeAnalyzer> attributeAnalyzers) {
    logger.info("Running attribute analyzers");
    int recordCount = records.size();
    final List<ResultSet> results = new ArrayList<>();
    final Map<String, List<Attribute>> attributes = AttributeAnalyzer.prepareRecords(records);
    for (AttributeAnalyzer attributeAnalyzer : attributeAnalyzers) {
      logger.info("Running analyzer: " + attributeAnalyzer.getName());
      attributeAnalyzer.setRecordCount(recordCount);
      results.add(attributeAnalyzer.analyze(attributes));
    }
    return results;
  }

  public void addAnalyzers(List<Analyzer> analyzers) {
    analyzers.forEach(this::addAnalyzer);
  }

  public void addAnalyzer(Analyzer analyzer) {
    if (analyzer instanceof RecordAnalyzer) {
      recordAnalyzers.add((RecordAnalyzer) analyzer);
    } else if (analyzer instanceof ClusterAnalyzer) {
      clusterAnalyzers.add((ClusterAnalyzer) analyzer);
    } else if (analyzer instanceof AttributeAnalyzer) {
      attributeAnalyzers.add((AttributeAnalyzer) analyzer);
    }
  }

  public void setRecordPreprocessor(
    RecordPreprocessor recordPreprocessor) {
    this.recordPreprocessor = recordPreprocessor;
  }

  public void setRunPerSource(boolean runPerSource) {
    this.runPerSource = runPerSource;
  }
}

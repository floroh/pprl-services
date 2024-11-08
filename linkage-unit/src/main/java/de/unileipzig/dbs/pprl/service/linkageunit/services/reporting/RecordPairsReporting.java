package de.unileipzig.dbs.pprl.service.linkageunit.services.reporting;

import de.unileipzig.dbs.pprl.core.analyzer.linking.SimilarityDistributionAnalyzer;
import de.unileipzig.dbs.pprl.core.analyzer.results.ResultSet;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import de.unileipzig.dbs.pprl.service.common.data.dto.reporting.Report;
import de.unileipzig.dbs.pprl.service.common.data.dto.reporting.ReportGroup;
import de.unileipzig.dbs.pprl.service.common.data.dto.reporting.ReportType;
import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoRecordPair;
import de.unileipzig.dbs.pprl.service.linkageunit.services.ProjectService;
import tech.tablesaw.api.LongColumn;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static de.unileipzig.dbs.pprl.core.matcher.model.api.LinkageProcessDataSet.ACTIVE;

public class RecordPairsReporting {

  public static final String REPORT_GROUP_NAME = "Record pairs";
  public static final String NUMBER_OF_PAIRS = "Number of pairs";
  public static final String MATCH_GRADE_COUNTS = "MatchGrade counts";
  public static final String PROPERTY_COUNTS = "Property counts";
  public static final String SIMILARITY_DISTRIBUTION = "Similarity distribution";

  public static ReportGroup createReportFromRecordPairs(Collection<RecordPair> recordPairs) {
    ReportGroup.ReportGroupBuilder builder = ReportGroup.builder()
      .name(REPORT_GROUP_NAME);

    Collection<RecordPair> activeRecordPairs = recordPairs.stream()
      .map(rp -> (MongoRecordPair) rp)
      .filter(rp -> rp.getProperties().contains(ACTIVE))
      .collect(Collectors.toList());
    builder.report(createReportForNumberOfPairs(recordPairs));
    builder.report(createReportFromCounts(recordPairs, activeRecordPairs,
      rp -> Collections.singleton(rp.getClassification()), "MatchGrade"
    ));
    builder.report(createReportFromCounts(recordPairs, activeRecordPairs,
      rp -> List.of(((MongoRecordPair) rp).getProperties()), "Property"
    ));
    builder.report(createReportFromSimilarityDistribution(
      ProjectService.removeReplacedRecordPairs(recordPairs)));
    return builder.build();
  }

  private static Report createReportForNumberOfPairs(Collection<RecordPair> recordPairs) {
    return Report.builder()
      .name(NUMBER_OF_PAIRS)
      .type(ReportType.TEXT)
      .report(String.valueOf(recordPairs.size()))
      .build();
  }

  private static Report createReportFromCounts(Collection<RecordPair> allRecordPairs,
    Collection<RecordPair> activeRecordPairs, Function<RecordPair, Collection<Object>> groupBy,
    String keyName) {
    Table tableAll = getGroupedCounts(allRecordPairs, groupBy, keyName);
    tableAll.column("count").setName("countAll");
    Table tableActive = getGroupedCounts(activeRecordPairs, groupBy, keyName);
    tableActive.column("count").setName("countActive");
    Table joinedTable = tableAll.joinOn(keyName).fullOuter(tableActive);
    joinedTable.forEach(r -> {
      if (r.isMissing("countAll")) {
        r.setLong("countAll", 0);
      }
      if (r.isMissing("countActive")) {
        r.setLong("countActive", 0);
      }
    });
    return Report.createTableReport(keyName + " counts", joinedTable);
  }

  private static Table getGroupedCounts(Collection<RecordPair> recordPairs,
    Function<RecordPair, Collection<Object>> groupBy, String keyName) {
    Map<Object, Long> matchGradeCounts = recordPairs.stream()
      .flatMap(rp -> groupBy.apply(rp).stream())
      .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    Table table = Table.create(
      keyName + " counts",
      StringColumn.create(keyName),
      LongColumn.create("count")
    );
    matchGradeCounts.forEach((mg, count) -> {
      Row row = table.appendRow();
      row.setString(keyName, mg.toString());
      row.setLong("count", count);
    });
    return table;
  }

  private static Report createReportFromSimilarityDistribution(Collection<RecordPair> recordPairs) {
    SimilarityDistributionAnalyzer analyzer = new SimilarityDistributionAnalyzer();
    analyzer.setBinSize(0.01);
    analyzer.setDynamicLowerBound(true);
    ResultSet resultSet = analyzer.analyze(recordPairs);
    return Report.createTableReport(SIMILARITY_DISTRIBUTION, resultSet.getAsTable());
  }
}

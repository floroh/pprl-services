package de.unileipzig.dbs.pprl.service.linkageunit.services.reporting;

import de.unileipzig.dbs.pprl.core.matcher.evaluation.GroundTruth;
import de.unileipzig.dbs.pprl.core.matcher.evaluation.QualityCheckImp;
import de.unileipzig.dbs.pprl.service.common.data.dto.reporting.Report;
import de.unileipzig.dbs.pprl.service.common.data.dto.reporting.ReportGroup;
import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoCluster;
import lombok.extern.slf4j.Slf4j;
import tech.tablesaw.api.IntColumn;
import tech.tablesaw.api.LongColumn;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class BlockingQualityReporting {

  public static final String REPORT_GROUP_NAME = "Blocking evaluation";
  private static final String POST_BLOCKING_QUALITY = "Post Blocking quality";
  public static final String BLOCK_SIZES = "Block sizes";
  public static final String BLOCKINGKEY_COUNTS = "Blockingkey counts";

  public static ReportGroup createReportGroup(Collection<MongoCluster> blockingCluster, GroundTruth gt) {
    log.debug("Creating blocking report for {} clusters", blockingCluster.size());
    QualityCheckImp qualityCheckImp = new QualityCheckImp(gt);

    ReportGroup.ReportGroupBuilder builder = ReportGroup.builder()
      .name(REPORT_GROUP_NAME);

//    ExtendedQualityResult extendedQualityResult =
//      (ExtendedQualityResult) qualityCheckImp.evaluateRecordPairs(recordPairs);

//    reportGroup.report(POST_BLOCKING_QUALITY, Report.createTableReport(POST_BLOCKING_QUALITY,
//    extendedQualityResult.getAsTable()));

    Map<Integer, Long> sizeCounts = blockingCluster.stream()
      .map(c -> c.getRecords().size())
      .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    Table table = Table.create(BLOCK_SIZES, IntColumn.create("size"), LongColumn.create("count"));
    sizeCounts.forEach((size, count) -> {
        Row row = table.appendRow();
        row.setInt("size", size);
        row.setLong("count", count);
      });
    builder.report(Report.createTableReport(BLOCK_SIZES, table));
    
    Map<Integer, Long> bkCounts = blockingCluster.stream()
      .map(c -> c.getBlockingKeys().size())
      .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    Table table2 = Table.create(BLOCKINGKEY_COUNTS, IntColumn.create("size"), LongColumn.create("count"));
    bkCounts.forEach((size, count) -> {
        Row row = table2.appendRow();
        row.setInt("size", size);
        row.setLong("count", count);
      });
    builder.report(Report.createTableReport(BLOCKINGKEY_COUNTS, table2));

    // TODO overlap of blocking strategies (when multiple bkv are equal)
    // TODO reduction ratio
    // TODO if GT is available: pairs completeness, maximal achievable recall, minimal precision

    return builder.build();
  }
}

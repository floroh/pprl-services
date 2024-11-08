package de.unileipzig.dbs.pprl.service.linkageunit.services.reporting;

import de.unileipzig.dbs.pprl.core.common.model.api.RecordIdPair;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import de.unileipzig.dbs.pprl.core.matcher.model.api.LinkageProcessDataSet;
import de.unileipzig.dbs.pprl.core.matcher.postprocessing.LinksPostprocessor;
import de.unileipzig.dbs.pprl.core.matcher.evaluation.ExtendedQualityResult;
import de.unileipzig.dbs.pprl.core.matcher.evaluation.GroundTruth;
import de.unileipzig.dbs.pprl.core.matcher.evaluation.QualityCheckImp;
import de.unileipzig.dbs.pprl.core.matcher.evaluation.ThresholdEvaluation;
import de.unileipzig.dbs.pprl.service.common.data.dto.reporting.Report;
import de.unileipzig.dbs.pprl.service.common.data.dto.reporting.ReportGroup;
import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoRecordPair;
import de.unileipzig.dbs.pprl.core.common.TableSerialization;
import de.unileipzig.dbs.pprl.service.linkageunit.services.LinkImprovementService;
import lombok.extern.slf4j.Slf4j;
import tech.tablesaw.api.LongColumn;
import tech.tablesaw.api.Table;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static de.unileipzig.dbs.pprl.core.matcher.model.api.LinkageProcessDataSet.REPLACED;
import static de.unileipzig.dbs.pprl.core.matcher.model.api.LinkageProcessDataSet.TAG_REMOVED_BY_POSTPROCESSING;
import static de.unileipzig.dbs.pprl.service.linkageunit.services.LinkImprovementService.*;

@Slf4j
public class QualityEvaluationReporting {

  public static final String REPORT_GROUP_NAME = "Linkage quality evaluation";

  public static final String OVERVIEW = "Overview";
  public static final String ALL_PAIRS = "All pairs / Post blocking";
  public static final String ACTIVE = "Active";
  public static final String BEST = "Best";
  public static final String BEST_WITHOUT_IMPROVED = "Best (w/o improved)";
  public static final String THRESHOLDS = "Thresholds";
  public static final String THRESHOLDS_WITHOUT_IMPROVED = "Thresholds (w/o improved)";
  public static final String ACTIVE_LINKS_HISTORY = "Improved links history";
  public static final String LABELED_PAIRS = "Labeled pairs";
  public static final boolean evaluateThresholds = true;
  public static final boolean evaluateThresholdsWithoutImproved = true;

  public static final boolean includeLabeledPairs = false;

  private static LinksPostprocessor postprocessor;

  public static ReportGroup createReportGroup(Collection<RecordPair> recordPairs, GroundTruth gt,
    ReportGroup previousReportGroup) {
    QualityCheckImp qualityCheckImp = new QualityCheckImp(gt);

    ReportGroup.ReportGroupBuilder builder = ReportGroup.builder()
      .name(REPORT_GROUP_NAME);

    int totalCount = recordPairs.size();
    List<RecordPair> nonReplacedPairs = recordPairs.stream()
      .filter(rp -> !((MongoRecordPair) rp).getProperties().contains(REPLACED))
      .collect(Collectors.toList());
    Table qualityTable = getQualityResult(qualityCheckImp, nonReplacedPairs, (rp -> true)).
      getAsTableWithDescription(ALL_PAIRS);

    ExtendedQualityResult bestThresholdResult = null;
    if (evaluateThresholds) {
      List<RecordPair> nonReplacedPostProcessing = nonReplacedPairs.stream()
        .filter(rp -> rp.getTags().stream().noneMatch(t -> TAG_REMOVED_BY_POSTPROCESSING.equals((t.getTag())))
        ).toList();
      Table thresholdResults = new ThresholdEvaluation().evalThresholds(nonReplacedPostProcessing, qualityCheckImp,
        postprocessor
      );
      double bestThreshold = ThresholdEvaluation.getBestThreshold(thresholdResults);
      log.info("Best threshold: " + bestThreshold);
      bestThresholdResult = ThresholdEvaluation.evalThreshold(nonReplacedPostProcessing, qualityCheckImp, bestThreshold,
        postprocessor
      );
      qualityTable.append(bestThresholdResult.getAsTableWithDescription(BEST + " (" + bestThreshold + ")"));
      builder.report(Report.createTableReport(THRESHOLDS, thresholdResults));
    }
    if (evaluateThresholdsWithoutImproved) {
      Set<String> improvedPairIds = nonReplacedPairs.stream()
        .filter(rp -> ((MongoRecordPair) rp).getProperties().contains(PROPERTY_IMPROVED_LINK))
        .map(RecordIdPair::getPairId)
        .collect(Collectors.toSet());
      List<RecordPair> nonImprovedPairs = nonReplacedPairs.stream()
        .filter(rp -> !((MongoRecordPair) rp).getProperties().contains(PROPERTY_IMPROVED_LINK))
        .collect(Collectors.toList());
      if (!improvedPairIds.isEmpty() && !nonImprovedPairs.isEmpty()) {
        List<RecordIdPair> gtIdPairsWithoutImproved = gt.getIdPairs().stream()
          .filter(idPair -> !improvedPairIds.contains(idPair.getPairId()))
          .toList();
        GroundTruth gtWithoutImproved = GroundTruth.createFromLinks(gtIdPairsWithoutImproved);
        QualityCheckImp qualityCheckWithoutImproved = new QualityCheckImp(gtWithoutImproved);
        Table thresholdResults =
          new ThresholdEvaluation().evalThresholds(nonImprovedPairs, qualityCheckWithoutImproved,
            postprocessor
          );
        double bestThreshold = 0;
        try {
          bestThreshold = ThresholdEvaluation.getBestThreshold(thresholdResults);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        log.info("Best threshold (without improved pairs): " + bestThreshold);
        ExtendedQualityResult bestThresholdResultWithoutImproved =
          ThresholdEvaluation.evalThreshold(nonImprovedPairs,
            qualityCheckWithoutImproved, bestThreshold,
            postprocessor
          );
        qualityTable.append(bestThresholdResultWithoutImproved.getAsTableWithDescription(
          BEST_WITHOUT_IMPROVED + " (" + bestThreshold + ")"));
        builder.report(Report.createTableReport(THRESHOLDS_WITHOUT_IMPROVED, thresholdResults));
      } else {
        log.info("No or all pairs are IMPROVED, skipping threshold evaluation without improved" +
          " pairs");
      }
    }

    List<RecordPair> activePairs = filter(
      nonReplacedPairs,
      (rp -> ((MongoRecordPair) rp).getProperties().contains(LinkageProcessDataSet.ACTIVE))
    );
    ExtendedQualityResult qualityResultActive = (ExtendedQualityResult) qualityCheckImp.evaluateRecordPairs(activePairs);
    qualityTable.append(qualityResultActive.getAsTableWithDescription(ACTIVE));

    Table activePairsResultTable = qualityResultActive.getAsTable();
    builder.report(Report.createTableReport(ACTIVE, activePairsResultTable));

    builder.report(Report.createTableReport(OVERVIEW, qualityTable));

    if (includeLabeledPairs) {
      builder.report(Report.createTableReport(LABELED_PAIRS, qualityResultActive.getResults()));
    }

    long nonReplacedCount = nonReplacedPairs.size();
    long improvedCount = nonReplacedPairs.stream()
      .filter(rp -> ((MongoRecordPair) rp).getProperties().contains(PROPERTY_IMPROVED_LINK))
      .count();
    if (improvedCount == 0) {
      long reportedCount = recordPairs.stream()
        .filter(rp -> ((MongoRecordPair) rp).getProperties().contains(PROPERTY_REPORTED_LINK))
        .filter(rp -> !((MongoRecordPair) rp).getProperties().contains(PROPERTY_LINK_FROM_UPPER_LAYER))
        .count();
      if (reportedCount > 0) {
        log.debug("Using reported count as improved count");
        improvedCount = reportedCount;
      }
    }
    long ppcrCount = recordPairs.stream()
      .filter(rp -> rp.getTags().stream()
        .filter(tag -> tag.getTag().equals(LinkImprovementService.TAG_ENCODING_METHOD))
        .anyMatch(tag -> tag.getStringValue().contains("PPCR"))
      ).count();
    log.debug("PPCR count: " + ppcrCount);
    Table activeLinksHistoryResultTable = createActiveLinksHistoryResultTable(activePairsResultTable,
      totalCount, nonReplacedCount, improvedCount, ppcrCount
    );
    if (previousReportGroup != null) {
      log.info("Appending to active links history");
      Report previousReport = previousReportGroup.getReports().get(ACTIVE_LINKS_HISTORY);
      Table result = TableSerialization.fromDefaultSerializableTable(previousReport.getTable());
      result.append(activeLinksHistoryResultTable);
      activeLinksHistoryResultTable = result;
    } else {
      if (bestThresholdResult != null) {
        Table bestResult = createActiveLinksHistoryResultTable(bestThresholdResult.getAsTable(), totalCount, nonReplacedCount,-1, 0);
        bestResult.append(activeLinksHistoryResultTable);
        activeLinksHistoryResultTable = bestResult;
      }
    }
    builder.report(Report.createTableReport(ACTIVE_LINKS_HISTORY, activeLinksHistoryResultTable));
    return builder.build();
  }

  private static Table createActiveLinksHistoryResultTable(Table activePairsResultTable, long totalCount,
    long nonReplacedCount, long improvedCount, long ppcrCount) {
    Table newResult = activePairsResultTable.copy().insertColumn(0, LongColumn.create("#Pairs"));
    newResult.insertColumn(1, LongColumn.create("#Non-replaced"));
    newResult.insertColumn(2, LongColumn.create("#Improved"));
    newResult.insertColumn(3, LongColumn.create("#PPCR"));
    newResult.row(0).setLong("#Pairs", totalCount);
    newResult.row(0).setLong("#Non-replaced", nonReplacedCount);
    newResult.row(0).setLong("#Improved", improvedCount);
    newResult.row(0).setLong("#PPCR", ppcrCount);
    return newResult;
  }

  private static ExtendedQualityResult getQualityResult(QualityCheckImp qualityCheckImp,
    Collection<RecordPair> recordPairs,
    Predicate<RecordPair> filter) {
    ExtendedQualityResult qualityResult =
      (ExtendedQualityResult) qualityCheckImp.evaluateRecordPairs(filter(recordPairs, filter));
    return qualityResult;
  }

  private static List<RecordPair> filter(Collection<RecordPair> recordPairs,
    Predicate<RecordPair> filter) {
    return recordPairs.stream()
      .filter(filter)
      .collect(Collectors.toList());
  }

}

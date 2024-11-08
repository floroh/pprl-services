package de.unileipzig.dbs.pprl.core.matcher.evaluation;

import de.unileipzig.dbs.pprl.core.common.HelperUtils;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordId;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordIdPair;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import de.unileipzig.dbs.pprl.core.matcher.matcher.DefaultBatchMatcher;
import de.unileipzig.dbs.pprl.core.matcher.postprocessing.LinksPostprocessor;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.TrapezoidIntegrator;
import org.apache.commons.math3.analysis.integration.UnivariateIntegrator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.Table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.OptionalDouble;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class ThresholdEvaluation {

  private static Logger logger = LogManager.getLogger();

  public static final String THRESHOLD_COLUMN = "threshold";

  private static double stepSize = 0.01;

  private static List<Double> thresholds;

  public ThresholdEvaluation() {
  }

  public Table evalThresholds(
    Collection<RecordPair> pairs,
    QualityCheckImp qualityCheck,
    LinksPostprocessor postprocessor) {
    Table results = null;
    for (Double threshold : getThresholdsToTest(pairs)) {
      logger.debug("Evaluating with threshold: " + threshold);
      QualityResult qualityResult = evalThreshold(pairs, qualityCheck, threshold, postprocessor);
      Table asTable = qualityResult.getAsTable();
      asTable.insertColumn(0, DoubleColumn.create(THRESHOLD_COLUMN, threshold));
      if (asTable.columnNames().contains(QualityResult.CSV_HEADER_PR_AUC)) {
        asTable.removeColumns(QualityResult.CSV_HEADER_PR_AUC);
      }
      if (results == null) {
        results = asTable;
      } else {
        results.append(asTable);
      }
    }
    return results;
  }

  public static ExtendedQualityResult evalThreshold(Collection<RecordPair> pairs,
    QualityCheckImp qualityCheck,
    Double threshold,
    LinksPostprocessor postprocessor) {
    Collection<RecordPair> filteredPairs = pairs.parallelStream()
      .peek(pair -> {
        pair.getLeftRecord().getId().removeId(RecordId.GLOBAL_ID);
        pair.getRightRecord().getId().removeId(RecordId.GLOBAL_ID);
      })
      .filter(pair -> pair.getSimilarity() >= threshold)
      .collect(Collectors.toList());
    int postThresholdFilterSize = filteredPairs.size();
    logger.debug("Remaining links after threshold filtering: " + postThresholdFilterSize + "/" + pairs.size());
    filteredPairs = DefaultBatchMatcher.postprocess(postprocessor, filteredPairs);
    logger.debug("Remaining links after postprocessing: " + filteredPairs.size() + "/" + postThresholdFilterSize);
    List<RecordIdPair> recordIdPairs = filteredPairs.stream()
      .map(rp -> (RecordIdPair) rp)
      .collect(Collectors.toList());
    return qualityCheck.evaluatePairs(recordIdPairs);
  }

  public static double getBestThreshold(Table thresholdResults) {
    Table bestResultOnly = thresholdResults.sortDescendingOn(QualityResult.CSV_HEADER_F1).rows(0);
    return bestResultOnly.doubleColumn(THRESHOLD_COLUMN).get(0);
  }

  public static double calculateAreaUnderCurve(Table results) {
    Table sortedResults = results.sortOn(QualityCheck.RECALL);
    UnivariateFunction function = new DiscreteFunction(
      sortedResults.numberColumn(QualityCheck.RECALL).asDoubleArray(),
      sortedResults.numberColumn(QualityCheck.PRECISION).asDoubleArray()
    );

//    List<Double> x = IntStream.range(0, 100).mapToObj(i -> (double) i)
//      .map(d -> d / 100)
//      .collect(Collectors.toList());
//    List<Double> y = x.stream().map(function::value).collect(Collectors.toList());
//    showPrecisionRecallCurve(Table.create("Dummy", DoubleColumn.create(QualityCheck.RECALL, x),
//      DoubleColumn.create(QualityCheck.PRECISION, y)));

    UnivariateIntegrator integrator = new TrapezoidIntegrator();
    double auc = 0;
    try {
      auc = integrator.integrate(1000000, function, 0.0, 1.0);
    } catch (Exception e) {
      logger.error("Failed to calculate AUC: " + e);
      return auc;
    }
    return auc;
  }

  private List<Double> getThresholdsToTest(Collection<RecordPair> pairs) {
    List<Double> usedThresholds = new ArrayList<>();
    if (thresholds != null && !thresholds.isEmpty()) {
      usedThresholds = thresholds;
    } else {
      usedThresholds = determineThresholdsToTest(pairs);
    }
    logger.debug("Using the following thresholds: " + usedThresholds);
    return usedThresholds;
  }

  private List<Double> determineThresholdsToTest(Collection<RecordPair> pairs) {
    OptionalDouble optionalMin = pairs.parallelStream()
      .map(RecordPair::getSimilarity)
      .mapToDouble(d -> d)
      .min();
    if (optionalMin.isEmpty()) {
      return Collections.emptyList();
    }
    double lowestSimilarity = optionalMin.getAsDouble();
    SortedSet<Double> thresholds = new TreeSet<>();
    thresholds.add(HelperUtils.roundToTwoDigits(lowestSimilarity));
    double nextStep = Math.ceil(lowestSimilarity / stepSize) * stepSize;
    for (double thr = nextStep; thr <= 1.001; thr += stepSize) {
      thresholds.add(HelperUtils.roundToTwoDigits(thr));
    }
    return new ArrayList<>(thresholds);
  }



  public void setStepSize(double stepSize) {
    this.stepSize = stepSize;
  }

  public static class DiscreteFunction implements UnivariateFunction {

    private double[] xArray;
    private double[] yArray;

    public DiscreteFunction(double[] xArray, double[] yArray) {
      this.xArray = xArray;
      this.yArray = yArray;
    }

    @Override
    public double value(double x) {
      int index = 0;
      for (double xVal : xArray) {
        if (x <= xVal) {
          break;
        }
        index++;
      }
      if (index == 0) {
        return yArray[index];
      } else if (index >= yArray.length) {
        return 0;
//        return yArray[yArray.length - 1];
      }
      double xRange = xArray[index - 1] - xArray[index];
      double xDiff = x - xArray[index - 1];
      double yRange = yArray[index - 1] - yArray[index];
      return yArray[index - 1] + (yRange * xDiff) / xRange;
    }
  }

  public static void setThresholds(double low, double high) {
    ArrayList<Double> thresholds = new ArrayList<>();
    for (double d = low; d < high + 0.001; d = d + stepSize) {
      thresholds.add(roundDouble(d));
    }
    setThresholds(thresholds);
  }

  public static void setThresholds(List<Double> thresholds) {
    ThresholdEvaluation.thresholds = thresholds;
  }

  private static double roundDouble(Double d) {
    return Double.parseDouble(String.format(Locale.ENGLISH, "%.2f", d));
  }

}

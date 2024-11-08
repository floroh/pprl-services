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

package de.unileipzig.dbs.pprl.core.matcher.classification;

import de.unileipzig.dbs.pprl.core.common.HelperUtils;
import de.unileipzig.dbs.pprl.core.common.TableSerialization;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import de.unileipzig.dbs.pprl.core.common.model.impl.SerializableTable;
import de.unileipzig.dbs.pprl.core.matcher.classification.model.InstanceWeightMethod;
import de.unileipzig.dbs.pprl.core.matcher.evaluation.QualityResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.IntStream;

import static de.unileipzig.dbs.pprl.core.matcher.MatcherUtils.getLabel;

/**
 * Simple {@link TrainableClassifier} that uses a single threshold value
 */
public class TrainableThresholdClassifier extends SingleThresholdClassifier implements TrainableClassifier {

  public static final String CUM_COUNT_LEFT = "cumCountLeft";
  public static final String CUM_COUNT_RIGHT = "cumCountRight";
  public static final String COLUMN_BIN_LEFT = "binLeft";
  public static final String COLUMN_BIN_RIGHT = "binRight";
  private Logger logger = LogManager.getLogger();
  private double originalThreshold = Double.NaN;

  private double maximalShift = 0.10;

  private double shiftPerUpdate = 0.02;

  private boolean useGlobalDistribution = false;

  private InstanceWeightMethod instanceWeightMethod = InstanceWeightMethod.PROBABILITY;

  private Map<Label, Collection<WeightedSimilarity>> similaritiesByLabel;

  private List<SerializableTable> qualityResultsByIteration;

  private SerializableTable similarityDistribution;

  public TrainableThresholdClassifier(double threshold) {
    super(threshold);
  }

  private TrainableThresholdClassifier() {
    super();
  }

  @Override
  public void fit(Collection<RecordPair> recordPairs) {
    logger.info("Fitting threshold...");
    similaritiesByLabel = null;
    initUpdater();
    recordPairs.forEach(this::addToSimilarities);
    updateThreshold();
  }

  @Override
  public void update(RecordPair newRecordPair) {
    initUpdater();
    addToSimilarities(newRecordPair);
    updateThreshold();
  }

  @Override
  public void update(Collection<RecordPair> newRecordPairs) {
    initUpdater();
    newRecordPairs.forEach(this::addToSimilarities);
    updateThreshold();
  }

  private static Double getUpdateMeasure(QualityResult qualityResult) {
//    return qualityResult.getWeightedF1Score();
    return qualityResult.getWeightedAccuracy();
  }

  private void updateThreshold() {
    String similarityStats = getSimilarityDescriptionString();
    logger.info("Updating threshold based on {} labeled instances ({})",
      similaritiesByLabel.values().stream().flatMapToInt(c -> IntStream.of(c.size())).sum(), similarityStats
    );
    logger.info("Using global distribution: {}", useGlobalDistribution);
    List<Double> thresholdsToTest = generateThresholdsToTest();
    double bestThreshold = getThreshold();
    sortByDistanceToBestThreshold(thresholdsToTest, bestThreshold);

    double bestThresholdWithoutGlobalDistribution = getBestThreshold(thresholdsToTest, bestThreshold, false);
    double bestThresholdWithGlobalDistribution = getBestThreshold(thresholdsToTest, bestThreshold, true);
    if (useGlobalDistribution) {
      bestThreshold = bestThresholdWithGlobalDistribution;
    } else {
      bestThreshold = bestThresholdWithoutGlobalDistribution;
    }
    setThreshold(bestThreshold);
  }

  private double getBestThreshold(List<Double> thresholdsToTest, double bestThreshold,
    boolean useGlobalDistribution) {
    SerializableTable tmpDist = getSimilarityDistribution();
    if (!useGlobalDistribution) {
      setSimilarityDistribution(null);
    }
    double bestScore = getUpdateMeasure(computeQualityResult(bestThreshold));
    logger.info("Old threshold: {} (score={})", round(bestThreshold, 2), round(bestScore, 3));
    Table qualityResultTable = null;
    for (double thresholdToTest : thresholdsToTest) {
      QualityResult qualityResult = computeQualityResult(thresholdToTest);
      Table asTableWithDescription = qualityResult.getAsTableWithDescription(round(thresholdToTest, 2));
      qualityResultTable = appendOrInitTable(qualityResultTable, asTableWithDescription);
      Double score = getUpdateMeasure(qualityResult);
      logger.debug("Threshold : {} (score={}, result={}", round(thresholdToTest, 2), round(score, 4),
        qualityResult
      );
      if (score > bestScore) {
        if (thresholdToTest > threshold - shiftPerUpdate - 0.001 &&
          thresholdToTest < threshold + shiftPerUpdate + 0.001) {
          bestThreshold = thresholdToTest;
          bestScore = score;
        }
      }
    }
    if (qualityResultsByIteration == null) {
      qualityResultsByIteration = new ArrayList<>();
    }
    qualityResultsByIteration.add(TableSerialization.toDefaultSerializableTable(qualityResultTable));
    logger.info("New threshold ({}): {} (score={})", useGlobalDistribution, round(bestThreshold, 2),
      round(bestScore, 4)
    );
    setSimilarityDistribution(tmpDist);
    return bestThreshold;
  }

  private String getSimilarityDescriptionString() {
    if (similaritiesByLabel == null) {
      initSimilaritiesByLabel();
    }
    String similarityStats = String.format(
      "%d TM, %d TNM, similarities = [%.3f, %.3f]",
      similaritiesByLabel.get(Label.TRUE_MATCH).size(),
      similaritiesByLabel.get(Label.TRUE_NON_MATCH).size(),
      similaritiesByLabel.values().stream().flatMap(Collection::stream)
        .map(WeightedSimilarity::getSimilarity).mapToDouble(d -> d).min().orElse(0.0),
      similaritiesByLabel.values().stream().flatMap(Collection::stream)
        .map(WeightedSimilarity::getSimilarity).mapToDouble(d -> d).max().orElse(0.0)
    );
    return similarityStats;
  }

  private static Table appendOrInitTable(Table qualityResultTable, Table asTableWithDescription) {
    if (qualityResultTable == null) {
      qualityResultTable = asTableWithDescription;
    } else {
      qualityResultTable.append(asTableWithDescription);
    }
    return qualityResultTable;
  }

  public static void sortByDistanceToBestThreshold(List<Double> thresholdsToTest, double bestThreshold) {
    thresholdsToTest.sort(
      Comparator.comparingDouble(o -> Math.abs(o - bestThreshold)));
  }

  private static String round(double value, int digits) {
    return String.format(Locale.ENGLISH, "%." + digits + "f", value);
  }

  private QualityResult computeQualityResult(double thresholdToTest) {
    Collection<WeightedSimilarity> allTM = similaritiesByLabel.get(Label.TRUE_MATCH);
    Collection<WeightedSimilarity> allTNM = similaritiesByLabel.get(Label.TRUE_NON_MATCH);
    long tp = allTM.stream()
      .filter(d -> testSimilarity(d.getSimilarity(), thresholdToTest))
      .count();
    long fn = allTM.size() - tp;
    long fp = allTNM.stream()
      .filter(d -> testSimilarity(d.getSimilarity(), thresholdToTest))
      .count();
    long tn = allTNM.size() - fp;

    double wtp = allTM.stream()
      .filter(d -> testSimilarity(d.getSimilarity(), thresholdToTest))
      .mapToDouble(WeightedSimilarity::getWeight)
      .sum();
    double wfn = allTM.stream()
      .filter(d -> !testSimilarity(d.getSimilarity(), thresholdToTest))
      .mapToDouble(WeightedSimilarity::getWeight)
      .sum();
    double wfp = allTNM.stream()
      .filter(d -> testSimilarity(d.getSimilarity(), thresholdToTest))
      .mapToDouble(WeightedSimilarity::getWeight)
      .sum();
    double wtn = allTNM.stream()
      .filter(d -> !testSimilarity(d.getSimilarity(), thresholdToTest))
      .mapToDouble(WeightedSimilarity::getWeight)
      .sum();

    QualityResult qualityResult = new QualityResult(tp, fp, fn);
    qualityResult.setTrueNeg(tn);
    qualityResult.setWeightedTruePos(wtp);
    qualityResult.setWeightedTrueNeg(wtn);
    qualityResult.setWeightedFalsePos(wfp);
    qualityResult.setWeightedFalseNeg(wfn);
    if (similarityDistribution != null) {
      Table dist = new TableSerialization().fromSerializableTable(similarityDistribution);
      if (!dist.containsColumn(CUM_COUNT_LEFT)) {
        prepareSimilarityDistributionTable(dist);
//        System.out.println(dist.printAll());
        similarityDistribution = TableSerialization.toDefaultSerializableTable(dist);
      }
      scaleQualityResultToGlobalDistribution(qualityResult, thresholdToTest, tn + fn, tp + fp, dist);
    }

    return qualityResult;
//    return score;
  }

  public static Table prepareSimilarityDistributionTable(Table dist) {
    DoubleColumn colCumCountLeft = dist.doubleColumn("count").cumSum().setName(CUM_COUNT_LEFT);
    List<Double> cumCountLeft = colCumCountLeft.asList();
    cumCountLeft.addFirst(0.0);
    cumCountLeft.removeLast();
    colCumCountLeft = DoubleColumn.create(CUM_COUNT_LEFT, cumCountLeft);

    DoubleColumn colCumCountRight =
      reverseColumnEntryOrder(
        reverseColumnEntryOrder(dist.doubleColumn("count")).cumSum().setName(CUM_COUNT_RIGHT));
    DoubleColumn colBinLeft = DoubleColumn.create(COLUMN_BIN_LEFT, dist.stringColumn("bin").asList().stream()
      .map(s -> s.split("-")[0]).mapToDouble(Double::parseDouble));

    DoubleColumn colBinRight = DoubleColumn.create(
      COLUMN_BIN_RIGHT,
      dist.stringColumn("bin").asList().stream().map(s -> s.split("-")[1]).mapToDouble(Double::parseDouble)
    );

    dist.addColumns(colBinLeft, colBinRight, colCumCountLeft, colCumCountRight);
    return dist;
  }

  public QualityResult scaleQualityResultToGlobalDistribution(QualityResult in,
    double threshold, double partialLeftCumCount, double partialRightCumCount, Table dist) {
//    logger.warn("Scaling quality result {} to global distribution with", in);
//    logger.warn("threshold={}, partialLeftCumCount={}, partialRightCumCount={}", threshold,
//    partialLeftCumCount,
//      partialRightCumCount);
    for (Row row : dist.sortDescendingOn(COLUMN_BIN_LEFT)) {
      if (threshold > row.getDouble(COLUMN_BIN_LEFT)) {
        in.setTrueNeg((long) (in.getTrueNeg() * row.getDouble(CUM_COUNT_LEFT) / partialLeftCumCount));
        in.setFalseNeg((long) (in.getFalseNeg() * row.getDouble(CUM_COUNT_LEFT) / partialLeftCumCount));
        in.setTruePos((long) (in.getTruePos() * row.getDouble(CUM_COUNT_RIGHT) / partialRightCumCount));
        in.setFalsePos((long) (in.getFalsePos() * row.getDouble(CUM_COUNT_RIGHT) / partialRightCumCount));
        in.setWeightedTrueNeg(
          (long) (in.getWeightedTrueNeg() * row.getDouble(CUM_COUNT_LEFT) / partialLeftCumCount));
        in.setWeightedFalseNeg(
          (long) (in.getWeightedFalseNeg() * row.getDouble(CUM_COUNT_LEFT) / partialLeftCumCount));
        in.setWeightedTruePos(
          (long) (in.getWeightedTruePos() * row.getDouble(CUM_COUNT_RIGHT) / partialRightCumCount));
        in.setWeightedFalsePos(
          (long) (in.getWeightedFalsePos() * row.getDouble(CUM_COUNT_RIGHT) / partialRightCumCount));
        break;
      }
    }
//    logger.warn("Scaled quality result {}", in);
    return in;
  }

  public static DoubleColumn reverseColumnEntryOrder(DoubleColumn column) {
    DoubleColumn reversedColumn = column.emptyCopy();
    for (int i = column.size() - 1; i >= 0; i--) {
      reversedColumn.append(column.get(i));
    }
    return reversedColumn;
  }

  private List<Double> generateThresholdsToTest() {
    List<Double> thresholdsToTest = new ArrayList<>();
//    double lower = originalThreshold - maximalShift;
//    double upper = originalThreshold + maximalShift;
    double lower = Math.max(threshold - shiftPerUpdate, originalThreshold - maximalShift);
    double upper = Math.min(threshold + shiftPerUpdate, originalThreshold + maximalShift);
    double curThreshold = HelperUtils.roundToTwoDigits(lower);
    while (curThreshold <= upper + 0.001) {
      thresholdsToTest.add(HelperUtils.roundToTwoDigits(curThreshold));
      curThreshold += 0.01;
    }
    return thresholdsToTest;
  }

  public void clearSimilarities() {
    initSimilaritiesByLabel();
  }

  private void addToSimilarities(RecordPair rp) {
    double similarity = rp.getSimilarity();
    Label label = getLabel(rp).orElseThrow(() -> new RuntimeException("Missing ground truth tag in " + rp));
    double probability = InstanceWeightMethod.NONE.equals((instanceWeightMethod)) ? 1.0 :
      getProbabilityTag(rp).orElse(1.0);
    WeightedSimilarity instance = new WeightedSimilarity(similarity, probability);
    if (InstanceWeightMethod.WEIGHTED_PROBABILITY.equals(instanceWeightMethod)) {
      boolean isFromClericalReview = rp.getTags().stream()
        .filter(tag -> tag.getTag().equals("METHOD"))
        .anyMatch(tag -> tag.getStringValue().contains("CR"));
      if (isFromClericalReview) {
        instance.setWeight(2 * instance.getWeight());
      }
    }

    similaritiesByLabel.get(label).add(instance);
  }

  private void initUpdater() {
    if (Double.isNaN(originalThreshold)) {
      originalThreshold = getThreshold();
    }
    if (similaritiesByLabel == null) {
      initSimilaritiesByLabel();
    }
  }

  private void initSimilaritiesByLabel() {
    similaritiesByLabel = new HashMap<>();
    similaritiesByLabel.put(Label.TRUE_MATCH, new ArrayList<>());
    similaritiesByLabel.put(Label.TRUE_NON_MATCH, new ArrayList<>());
  }

  public double getMaximalShift() {
    return maximalShift;
  }

  public void setMaximalShift(double maximalShift) {
    this.maximalShift = maximalShift;
  }

  public double getShiftPerUpdate() {
    return shiftPerUpdate;
  }

  public void setShiftPerUpdate(double shiftPerUpdate) {
    this.shiftPerUpdate = shiftPerUpdate;
  }

  public double getOriginalThreshold() {
    return originalThreshold;
  }

  public void setOriginalThreshold(double originalThreshold) {
    this.originalThreshold = originalThreshold;
  }

  public Map<Label, Collection<WeightedSimilarity>> getSimilaritiesByLabel() {
    return similaritiesByLabel;
  }

  public List<SerializableTable> getQualityResultsByIteration() {
    return qualityResultsByIteration;
  }

  public InstanceWeightMethod getInstanceWeightMethod() {
    return instanceWeightMethod;
  }

  public SerializableTable getSimilarityDistribution() {
    return similarityDistribution;
  }

  public void setSimilarityDistribution(
    SerializableTable similarityDistribution) {
    this.similarityDistribution = similarityDistribution;
  }

  @Override
  public String toString() {
    return "TrainableThresholdClassifier{" +
      "threshold=" + threshold +
      ", originalThreshold=" + originalThreshold +
      ", maximalShift=" + maximalShift +
      ", shiftPerUpdate=" + shiftPerUpdate +
      ", similaritiesByLabel=(" + getSimilarityDescriptionString() + ")" +
      ", leftDistance=" + leftDistance +
      ", rightDistance=" + rightDistance +
      '}';
  }

}
package de.unileipzig.dbs.pprl.core.analyzer.linking;

import de.unileipzig.dbs.pprl.core.analyzer.results.Result;
import de.unileipzig.dbs.pprl.core.analyzer.results.ResultSet;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import org.apache.commons.math3.stat.Frequency;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SimilarityDistributionAnalyzer extends LinkAnalyzer {

  public static final String COLUMN_BIN_RANGE = "bin";
  public static final String BIN_BOUND_SEPARATOR = "-";
  public static final String COLUMN_COUNT = "count";
  private double lowerBound = 0.0;
  private double upperBound = 1.0;
  private double binSize = 0.05;
  private int scale = 2;
  private boolean dynamicLowerBound = false;

  @Override
  public ResultSet analyze(Collection<RecordPair> pairs) {
    List<Double> similarities = pairs.stream()
      .map(RecordPair::getSimilarity)
      .collect(Collectors.toList());
    return analyzeSimilarities(similarities);
  }

  public ResultSet analyzeSimilarities(Collection<Double> similarities) {
    Frequency frequency = new Frequency();
    similarities.stream()
      .map(this::toBD)
      .forEach(frequency::addValue);

    BigDecimal blowerBound = BigDecimal.valueOf(lowerBound);
    if (dynamicLowerBound) {
      blowerBound = BigDecimal.valueOf(similarities.stream().mapToDouble(d->d).min().orElse(lowerBound));
      blowerBound = blowerBound.setScale(scale, RoundingMode.DOWN);
    }
    ResultSet resultSet = new ResultSet("SimilarityDistribution");
    int numberOfBins = (int) (1.0 / binSize);

    double scaleSize = Math.pow(10, -scale);

    long cumFreqLower = 0;
    for (int i = 0; i < numberOfBins; i++) {
      BigDecimal curLowerBound = toBD((double) i / numberOfBins);
      BigDecimal curUpperBoundPrint = toBD((double) (i+1) / numberOfBins);
      BigDecimal curUpperBound = toBD((i == numberOfBins - 1) ?
        upperBound
        : (double) (i + 1) / numberOfBins - scaleSize
      );
      long cumFreqUpper = frequency.getCumFreq(curUpperBound);
      if (curLowerBound.compareTo(blowerBound) >= 0) {
        Result result = new Result();
        result.setParam(COLUMN_BIN_RANGE, curLowerBound + BIN_BOUND_SEPARATOR + curUpperBoundPrint);
        BigDecimal binCount = BigDecimal.valueOf(cumFreqUpper - cumFreqLower);
        result.addMetric(COLUMN_COUNT, binCount);
        resultSet.addResult(result);
      }
      cumFreqLower = cumFreqUpper;
    }
    return resultSet;
  }

  public double getBinSize() {
    return binSize;
  }

  public void setBinSize(double binSize) {
    this.binSize = binSize;
  }

  public boolean isDynamicLowerBound() {
    return dynamicLowerBound;
  }

  public void setDynamicLowerBound(boolean dynamicLowerBound) {
    this.dynamicLowerBound = dynamicLowerBound;
  }

  public double getLowerBound() {
    return lowerBound;
  }

  public void setLowerBound(double lowerBound) {
    this.lowerBound = lowerBound;
  }

  public double getUpperBound() {
    return upperBound;
  }

  public void setUpperBound(double upperBound) {
    this.upperBound = upperBound;
  }

  public int getScale() {
    return scale;
  }

  public void setScale(int scale) {
    this.scale = scale;
  }

  private BigDecimal toBD(double value) {
    BigDecimal bigDecimal = BigDecimal.valueOf(value);
    bigDecimal = bigDecimal.setScale(scale, RoundingMode.HALF_DOWN);
    return bigDecimal;
  }
}

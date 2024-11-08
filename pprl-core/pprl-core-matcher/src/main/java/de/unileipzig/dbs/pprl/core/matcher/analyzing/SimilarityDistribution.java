package de.unileipzig.dbs.pprl.core.matcher.analyzing;

import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.stream.Collectors;

public class SimilarityDistribution {

  public static final double MIN_DEFAULT = 0.5;
  public static final double MAX_DEFAULT = 1.0;
  public static final int NUM_BINS_DEFAULT = 20;

  private double min = MIN_DEFAULT;
  private double max = MAX_DEFAULT;
  private int numBins = NUM_BINS_DEFAULT;

  private final static Logger logger = LogManager.getLogger(SimilarityDistribution.class);

  private long[] result;

  public SimilarityDistribution(int numBins) {
    this.numBins = numBins;
  }

  public SimilarityDistribution() {
  }

  public void calcFromRecordPairs(Collection<RecordPair> recordPairs) {
    calcFromSimilarities(
      recordPairs.stream()
        .map(RecordPair::getSimilarity)
        .collect(Collectors.toList())
    );
  }

  public void calcFromSimilarities(Collection<Double> similarities) {
    final double binSize = (max - min) / numBins;
    result = new long[numBins];

    for (double d : similarities) {
      int bin = (int) ((d - min) / binSize); // changed this from numBins
      if (bin == numBins) bin = numBins - 1;

      if (bin < 0) {
        logger.warn("Found a similarity < " + min);
      } else if (bin >= numBins) {
        logger.warn("Found a similarity > " + max);
      } else {
        result[bin] += 1;
      }
    }
  }

  public long[] getResult() {
    return result;
  }

  public double getMin() {
    return min;
  }

  public void setMin(double min) {
    this.min = min;
  }

  public double getMax() {
    return max;
  }

  public void setMax(double max) {
    this.max = max;
  }

  public int getNumBins() {
    return numBins;
  }

  public void setNumBins(int numBins) {
    this.numBins = numBins;
  }
}

package de.unileipzig.dbs.pprl.core.common.frequencies;

public class WeightUtils {

  public static double getWeightDiff(double m, double u) {
    final double weightM = getWeightM(m, u);
    final double weightU = getWeightU(m, u);
    return weightM - weightU;
  }

  public static double getWeightM(double m, double u) {
    return Math.log(m / u) / Math.log(2);
  }

  public static double getWeightU(double m, double u) {
    return Math.log((1 - m) / (1 - u)) / Math.log(2);
  }

  public static double getProbM(double averageError) {
    return 1 - averageError;
  }

  public static double getProbU(double averageAttributeFrequency) {
    return averageAttributeFrequency;
  }
}

package de.unileipzig.dbs.pprl.service.generator.generation.germany.randomgenerator;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ProbabilityDistribution {

  @Setter
  private List<String> eventSpace_String;

  private int[] eventSpace_int;

  private int[][] eventSpace_intArray;

  @Setter
  private List<Double> probabilities;

  private ProbabilityDistribution(List<String> eventSpace_String, List<Double> probabilities) {
    this.eventSpace_String = eventSpace_String;
    this.probabilities = probabilities;
  }

  public static ProbabilityDistribution fromDoubleCounts(List<String> eventSpace, List<Double> counts) {
    return new ProbabilityDistribution(eventSpace, calculateProbabilities_FromDouble(counts));
  }

  public static ProbabilityDistribution fromStringCounts(List<String> eventSpace, List<String> counts) {
    return new ProbabilityDistribution(eventSpace, calculateProbabilities_FromString(counts));
  }

  public ProbabilityDistribution(int[] eventSpace_int, List<String> counts) {
    this.eventSpace_int = eventSpace_int;
    this.probabilities = calculateProbabilities_FromString(counts);
  }

  public ProbabilityDistribution(int[][] eventSpace_intArray, List<String> counts) {
    this.eventSpace_intArray = eventSpace_intArray;
    this.probabilities = calculateProbabilities_FromString(counts);
  }


  public ProbabilityDistribution(List<String> eventSpace_String, boolean uniform) {
    List<Double> probabilities = new ArrayList<>();
    double sum = eventSpace_String.size();
    for (String event : eventSpace_String) {
      probabilities.add(1.0 / sum);
    }
    this.eventSpace_String = eventSpace_String;
    this.probabilities = probabilities;
  }

  private static List<Double> calculateProbabilities_FromString(List<String> counts) {
    List<Double> probabilities = new ArrayList<>();
    double sum = 0;
    for (String s : counts) {
      sum += Double.parseDouble(s);
    }
    for (String s : counts) {
      probabilities.add(Double.parseDouble(s) / sum);
    }
    return probabilities;
  }

  private static List<Double> calculateProbabilities_FromDouble(List<Double> counts) {
    List<Double> probabilities = new ArrayList<>();
    double sum = 0;
    for (Double d : counts) {
      sum += d;
    }
    for (Double d : counts) {
      probabilities.add(d / sum);
    }
    return probabilities;
  }


}
package de.unileipzig.dbs.pprl.core.matcher.classification;

public class WeightedSimilarity {
  private double similarity;
  private double weight;

  public WeightedSimilarity(double similarity, double weight) {
    this.similarity = similarity;
    this.weight = weight;
  }

  private WeightedSimilarity() {
  }

  public double getSimilarity() {
    return similarity;
  }

  public void setSimilarity(double similarity) {
    this.similarity = similarity;
  }

  public double getWeight() {
    return weight;
  }

  public void setWeight(double weight) {
    this.weight = weight;
  }
}

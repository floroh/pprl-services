package de.unileipzig.dbs.pprl.core.common.frequencies;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import de.unileipzig.dbs.pprl.core.common.exceptions.ConfigurationException;

import java.util.Map;

public class DefaultWeightCalculator implements WeightCalculator {

  @JsonPropertyOrder(alphabetic = true)
  private Map<String, Double> defaultWeights;

  protected DefaultWeightCalculator() {
  }

  public DefaultWeightCalculator(Map<String, Double> defaultWeights) {
    this.defaultWeights = defaultWeights;
  }

  @Override
  public double getWeight(String attributeName, String attributeValue) {
    try {
      return defaultWeights.get(attributeName);
    } catch (Exception e) {
      throw new ConfigurationException("Missing default weight for attribute: " + attributeName + "." +
        " (Existing weights: " + defaultWeights + ")");
    }
  }

  @Override
  public double getScale(String attributeName, String attributeValue) {
    return 1.0;
  }

  public Map<String, Double> getDefaultWeights() {
    return defaultWeights;
  }

}

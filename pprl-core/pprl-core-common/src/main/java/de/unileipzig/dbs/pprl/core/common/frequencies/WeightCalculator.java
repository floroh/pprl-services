package de.unileipzig.dbs.pprl.core.common.frequencies;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property = "@class")
@JsonPropertyOrder(alphabetic=true)
public interface WeightCalculator {

  double getWeight(String attributeName, String attributeValue);

  double getScale(String attributeName, String attributeValue);
}

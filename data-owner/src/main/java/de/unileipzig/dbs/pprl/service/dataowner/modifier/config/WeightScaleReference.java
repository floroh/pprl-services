package de.unileipzig.dbs.pprl.service.dataowner.modifier.config;

import de.unileipzig.dbs.pprl.service.common.modifier.JsonModifier;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class WeightScaleReference extends ConfigModifier<BigDecimal> {
  public static final String KEY = "SREF";
  private static final String JSONPATH = "$.." +
    JsonModifier.classSelector("weightCalculator", "ScaledWeightCalculator") +
    ".relativeTop";
  private final List<BigDecimal> values;

  public WeightScaleReference(List<Double> values) {
    super(KEY);
    this.values = values.stream()
      .map(BigDecimal::valueOf)
      .collect(Collectors.toList());
  }

  @Override
  public List<ConfigVariant> modify(ConfigVariant config) {
    if (!JsonModifier.test(config.getConfig(), JSONPATH)) {
      ConfigPreparator.logger.warn("Tried to modify property (" + KEY + ") that does not exist");
      return new ArrayList<>();
    }

    return applyPropertyVariants(config, values);
  }

  @Override
  String changeProperty(String props, BigDecimal newValue) {
    props = JsonModifier.set(props, JSONPATH, Double.valueOf(format(newValue)));
    return props;
  }

  @Override
  String format(BigDecimal newValue) {
//		return newValue.toString();
    return String.format(Locale.ENGLISH, "%.2f", newValue);
  }

  @Override
  public String toString() {
    return "WeightScaleReference{" +
      "values=" + values +
      '}';
  }
}

package de.unileipzig.dbs.pprl.service.dataowner.modifier.config;

import de.unileipzig.dbs.pprl.service.common.modifier.JsonModifier;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class CsvAttributeFrequencyLimit extends ConfigModifier<BigDecimal> {
  public static final String KEY = "FREQLIM";
  public enum LimitType {ABSOLUTE_TOP, RELATIVE_TOP, ABSOLUTE_BOTTOM, RELATIVE_BOTTOM}

  private static final String JSONPATH = "$.." +
    JsonModifier.classSelector("frequencyLookupProvider", "CsvAttributesFrequencyLookupProvider") +
    ".filter";
  private final LimitType limitType;
  private final List<BigDecimal> values;

  public CsvAttributeFrequencyLimit(Map<String, String> params) {
    super(buildParamKey(params.get("limitName")));
    this.limitType = LimitType.valueOf(params.get("limitName"));
    throw new RuntimeException("Parsing param lists not implemented yet");
  }

  public CsvAttributeFrequencyLimit(LimitType limitType, List<Double> values) {
    super(buildParamKey(limitType.name()));
    this.limitType = limitType;
    this.values = values.stream()
      .map(BigDecimal::valueOf)
      .collect(Collectors.toList());
  }

  private String getJsonKey() {
    return switch (limitType) {
      case ABSOLUTE_TOP -> "absoluteTopLimit";
      case RELATIVE_TOP -> "relativeTopLimit";
      case ABSOLUTE_BOTTOM -> "absoluteBottomLimit";
      case RELATIVE_BOTTOM -> "relativeBottomLimit";
      default -> throw new RuntimeException("Unsupported limitType: " + limitType);
    };
  }

  private static String buildParamKey(String limitType) {
    return switch (LimitType.valueOf(limitType)) {
      case ABSOLUTE_TOP -> KEY + "ABSTOP";
      case RELATIVE_TOP -> KEY + "RELTOP";
      case ABSOLUTE_BOTTOM -> KEY + "ABSBOT";
      case RELATIVE_BOTTOM -> KEY + "RELBOT";
      default -> throw new RuntimeException("Unsupported limitType: " + limitType);
    };
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
    Object value = Double.valueOf(format(newValue));
    if (limitType.equals(LimitType.ABSOLUTE_TOP) || limitType.equals(LimitType.ABSOLUTE_BOTTOM)) {
      value = ((Double)value).longValue();
    }
    props = JsonModifier.put(props, JSONPATH, getJsonKey(), value);
    return props;
  }

  @Override
  String format(BigDecimal newValue) {
//		return newValue.toString();
    return String.format(Locale.ENGLISH, "%.2f", newValue);
  }

  @Override
  public String toString() {
    return "CsvAttributeFrequencyLimit{" +
      "limitName='" + limitType + '\'' +
      ", values=" + values +
      '}';
  }
}

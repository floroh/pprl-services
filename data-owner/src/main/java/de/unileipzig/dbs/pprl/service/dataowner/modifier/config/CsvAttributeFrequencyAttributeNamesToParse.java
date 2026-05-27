package de.unileipzig.dbs.pprl.service.dataowner.modifier.config;

import de.unileipzig.dbs.pprl.core.common.model.impl.PersonalAttributeType;
import de.unileipzig.dbs.pprl.service.common.modifier.JsonModifier;

import java.util.ArrayList;
import java.util.List;

public class CsvAttributeFrequencyAttributeNamesToParse extends ConfigModifier<List<String>> {
  public static final String KEY = "FREQATTR";

  private static final String JSONPATH = "$.." +
    JsonModifier.classSelector("frequencyLookupProvider", "CsvAttributesFrequencyLookupProvider") +
    ".attributesNamesToParse";

  private final List<List<String>> values;

  public CsvAttributeFrequencyAttributeNamesToParse(List<List<String>> attributeNamesToParse) {
    super(KEY);
    this.values = attributeNamesToParse;
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
  String changeProperty(String props, List<String> newValue) {
    props = JsonModifier.set(props, JSONPATH, newValue);
    return props;
  }

  @Override
  String format(List<String> newValue) {
    return buildParamName(newValue);
  }

  public static String buildParamName(List<String> attributeNamesToParse) {
    StringBuilder sb = new StringBuilder();
    for (String attributeNameToParse : attributeNamesToParse) {
      sb.append("_").append(getShortName(attributeNameToParse));
    }
    return sb.toString();
  }

  private static String getShortName(String attributeNameToParse) {
    try {
      PersonalAttributeType type = PersonalAttributeType.valueOf(attributeNameToParse);
      return type.getShortName();
    } catch (Exception e) {
      return attributeNameToParse;
    }
  }

  @Override
  public String toString() {
    return "CsvAttributeFrequencyAttributeNamesToParse{" +
      "attributeNamesToParse=" + values +
      '}';
  }
}

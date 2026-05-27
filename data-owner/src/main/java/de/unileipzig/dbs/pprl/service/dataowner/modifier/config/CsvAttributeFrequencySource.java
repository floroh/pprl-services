package de.unileipzig.dbs.pprl.service.dataowner.modifier.config;


import de.unileipzig.dbs.pprl.service.common.modifier.JsonModifier;

import java.util.*;

public class CsvAttributeFrequencySource extends ConfigModifier<String> {
  public static final String KEY = "FREQSRC";

  private static final String JSONPATH = "$.." +
    JsonModifier.classSelector("frequencyLookupProvider", "CsvAttributesFrequencyLookupProvider") +
    ".location";
  private final Map<String, String> locations;

  public CsvAttributeFrequencySource(Map<String, String> params) {
    super(KEY);
    throw new RuntimeException("Parsing param lists not implemented yet");
  }

  public CsvAttributeFrequencySource(String shortName, String location) {
    super(KEY);
    this.locations = new HashMap<>();
    this.locations.put(shortName, location);
  }

  public CsvAttributeFrequencySource addLocation(String shortName, String location) {
    locations.put(shortName, location);
    return this;
  }

  @Override
  public List<ConfigVariant> modify(ConfigVariant config) {
    if (!JsonModifier.test(config.getConfig(), JSONPATH)) {
      ConfigPreparator.logger.warn("Tried to modify property (" + KEY + ") that does not exist");
      return new ArrayList<>();
    }

    return applyPropertyVariants(config, new ArrayList<>(locations.values()));
  }

  @Override
  String changeProperty(String props, String newValue) {
    props = JsonModifier.set(props, JSONPATH, newValue);
    return props;
  }

  @Override
  String format(String newValue) {
    Optional<String> shortName = locations.entrySet().stream()
      .filter(e -> e.getValue().equals(newValue))
      .map(Map.Entry::getKey)
      .findFirst();
    if (shortName.isEmpty()) throw new RuntimeException("Something went wrong...");
    return shortName.get();
  }

  @Override
  public String toString() {
    return "CsvAttributeFrequencySource{" +
      "locations=" + locations +
      '}';
  }
}

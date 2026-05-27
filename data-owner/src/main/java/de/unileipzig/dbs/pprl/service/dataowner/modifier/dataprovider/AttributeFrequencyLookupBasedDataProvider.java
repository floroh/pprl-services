package de.unileipzig.dbs.pprl.service.dataowner.modifier.dataprovider;

import de.unileipzig.dbs.pprl.core.common.frequencies.AttributeFrequencyLookup;
import de.unileipzig.dbs.pprl.core.common.frequencies.AttributesFrequencyLookup;
import de.unileipzig.dbs.pprl.core.common.frequencies.AttributesFrequencyLookupFilter;
import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Provide data based attributes and their frequencies from {@link AttributesFrequencyLookup}
 */
@Log4j2
public class AttributeFrequencyLookupBasedDataProvider implements GeneratorDataProvider {

  private Random random;
  private AttributesFrequencyLookup lookup;

  public AttributeFrequencyLookupBasedDataProvider(
    AttributesFrequencyLookup lookup) {
    this.lookup = lookup;
    this.random = new Random();
  }
  public AttributeFrequencyLookupBasedDataProvider(
    AttributesFrequencyLookup lookup, long seed) {
    this.lookup = lookup;
    this.random = new Random(seed);
  }

  @Override
  public List<String> getAllValues(String attributeName, boolean distinct) {
    AttributeFrequencyLookup attrAfl = getAttributeLookup(attributeName);
    if (distinct) {
      return new ArrayList<>(attrAfl.getFrequencies().keySet());
    }
    return generateValuesBasedOnFrequencies(attrAfl.getFrequencies());
  }

  @Override
  public List<String> getFrequencyFilteredValues(String attributeName, boolean isRare, double share,
    boolean distinct) {
    AttributesFrequencyLookupFilter filter = new AttributesFrequencyLookupFilter();
    if (isRare) {
      filter.setRelativeBottomLimit(share);
    } else {
      filter.setRelativeTopLimit(share);
    }
    AttributeFrequencyLookup attributeLookup = getAttributeLookup(attributeName);
    AttributeFrequencyLookup filteredLookup = filter.filter(attributeLookup);
    if (distinct) {
      return new ArrayList<>(filteredLookup.getFrequencies().keySet());
    }
    return generateValuesBasedOnFrequencies(filteredLookup.getFrequencies());
  }

  private AttributeFrequencyLookup getAttributeLookup(String attributeName) {
    return lookup.getAttributeFrequencyLookup(attributeName).get();
  }

  private static List<String> generateValuesBasedOnFrequencies(Map<String, Long> frequencies) {
    List<String> values = frequencies.entrySet().parallelStream()
      .flatMap(e -> Collections.nCopies(e.getValue().intValue(), e.getKey()).stream())
      .collect(Collectors.toList());
    Collections.shuffle(values);
    return values;
  }

}

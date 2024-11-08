package de.unileipzig.dbs.pprl.core.common.frequencies;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.unileipzig.dbs.pprl.core.common.model.impl.PersonalAttributeType;
import de.unileipzig.dbs.pprl.core.common.preprocessing.StringAttributePreprocessor;
import de.unileipzig.dbs.pprl.core.common.preprocessing.StringEncoder;
import de.unileipzig.dbs.pprl.core.common.preprocessing.StringNormalizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class AttributesFrequencyLookup {

  private boolean transformAttributes = false;

  private static final StringAttributePreprocessor preprocessor = new StringNormalizer(
    true, false, true, true);

  private static final StringEncoder soundexEncoder = StringEncoder.SOUNDEX;

  private Map<String, AttributeFrequencyLookup> attributeFrequencyLookups = new HashMap<>();

  public AttributesFrequencyLookup(
    Map<String, AttributeFrequencyLookup> attributeFrequencyLookups) {
    this.attributeFrequencyLookups = attributeFrequencyLookups;
  }

  public AttributesFrequencyLookup(boolean transformAttributes) {
    this.transformAttributes = transformAttributes;
  }

  public Optional<Double> getRelativeFrequency(String attributeName, String attributeValue) {
    return getAttributeFrequencyLookup(attributeName)
      .flatMap(afl -> afl.getRelativeFrequency(normalizeAttributeValue(attributeName, attributeValue)));
  }

  public Optional<Double> getRelativeRank(String attributeName, String attributeValue) {
    return getAttributeFrequencyLookup(attributeName)
      .flatMap(afl -> afl.getRelativeRank(normalizeAttributeValue(attributeName, attributeValue)));
  }

  public Optional<Long> getFrequency(String attributeName, String attributeValue) {
    AttributeFrequencyLookup attributeFrequencyLookup = attributeFrequencyLookups.get(attributeName);
    if (attributeFrequencyLookup == null) {
      return Optional.empty();
    }
    return attributeFrequencyLookup.getFrequency(normalizeAttributeValue(attributeName, attributeValue));
  }

  public List<String> getAttributes() {
    return new ArrayList<>(attributeFrequencyLookups.keySet());
  }

  public Optional<AttributeFrequencyLookup> getAttributeFrequencyLookup(String attributeName) {
    return Optional.ofNullable(attributeFrequencyLookups.get(attributeName));
  }

  public AttributesFrequencyLookup addAttributeFrequencyLookup(String attributeName,
    AttributeFrequencyLookup lookup) {
    attributeFrequencyLookups.put(attributeName, lookup);
    return this;
  }

  public Long getTotalCount(String attributeName) {
    AttributeFrequencyLookup attributeFrequencyLookup = attributeFrequencyLookups.get(attributeName);
    if (attributeFrequencyLookup == null) {
      return 0L;
    }
    return attributeFrequencyLookup.getTotalCount();
  }

  public Long getUniqueCount(String attributeName) {
    AttributeFrequencyLookup attributeFrequencyLookup = attributeFrequencyLookups.get(attributeName);
    if (attributeFrequencyLookup == null) {
      return 0L;
    }
    return attributeFrequencyLookup.getUniqueCount();
  }

  public Long getHighestFrequency(String attributeName) {
    AttributeFrequencyLookup attributeFrequencyLookup = attributeFrequencyLookups.get(attributeName);
    if (attributeFrequencyLookup == null) {
      return 0L;
    }
    return attributeFrequencyLookup.getHighestFrequency();
  }

  public Long getLowestFrequency(String attributeName) {
    AttributeFrequencyLookup attributeFrequencyLookup = attributeFrequencyLookups.get(attributeName);
    if (attributeFrequencyLookup == null) {
      return 0L;
    }
    return attributeFrequencyLookup.getLowestFrequency();
  }

  public String normalizeAttributeValue(String attributeName, String input) {
    String normalized = preprocessor.preprocess(input);
    if (transformAttributes) {
      if (List.of(
        PersonalAttributeType.FIRSTNAME.asString(),
        PersonalAttributeType.LASTNAME.asString()
      ).contains(attributeName)) {
        normalized = soundexEncoder.preprocess(normalized);
      }
    }
//    return input.toUpperCase();
    return normalized;
  }
}

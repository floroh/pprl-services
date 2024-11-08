package de.unileipzig.dbs.pprl.core.encoder.attribute;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.unileipzig.dbs.pprl.core.common.factories.AttributeFactory;
import de.unileipzig.dbs.pprl.core.common.frequencies.AttributesFrequencyLookup;
import de.unileipzig.dbs.pprl.core.common.frequencies.AttributesFrequencyLookupProvider;
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.ListAttribute;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.preprocessing.RecordPreprocessor;
import de.unileipzig.dbs.pprl.core.encoder.hardening.Hardener;
import de.unileipzig.dbs.pprl.core.encoder.model.NamedAttribute;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class AttributeFrequencyEncoderGroup implements AttributeEncoderGroup<String> {

  public static final String SUFFIX_RELATIVE_FREQUENCY = "_RELFREQ";
  public static final String SUFFIX_RELATIVE_RANK = "_RELRANK";
  public static final String SUFFIX_FREQUENCY_LABEL = "_FRQLABEL";
  private AttributesFrequencyLookupProvider frequencyLookupProvider;

  @JsonIgnore
  private AttributesFrequencyLookup frequencyLookup;

  private List<String> attributeNames;

  private boolean includeRelativeFrequency = true;
  private boolean includeRelativeRank = true;
  private boolean includeFrequencyLabel = true;

  private static final Logger logger = LogManager.getLogger(AttributeFrequencyEncoderGroup.class);

  public AttributeFrequencyEncoderGroup(AttributesFrequencyLookupProvider frequencyLookupProvider,
    List<String> attributeNames) {
    this.frequencyLookupProvider = frequencyLookupProvider;
    this.attributeNames = attributeNames;
  }

  private AttributeFrequencyEncoderGroup() {
  }

  private void init() {
    if (frequencyLookup == null) {
      frequencyLookup = frequencyLookupProvider.provide();
    }
  }

  @Override
  public List<NamedAttribute> encode(Record record) {
    init();
    List<NamedAttribute> frequencyAttributes = new ArrayList<>();

    for (String attributeName : attributeNames) {
      Optional<Attribute> optionalAttribute = record.getAttribute(attributeName);
      if (optionalAttribute.isEmpty()) {
//        logger.warn("Attribute with name {} not found in record.", attributeName);
        continue;
      }
      Attribute attribute = optionalAttribute.get();
      if (attribute instanceof ListAttribute) {
        logger.warn("List attribute are not supported");
        continue;
      }
      if (includeRelativeFrequency) {
        frequencyLookup.getRelativeFrequency(attributeName, attribute.getAsString())
          .ifPresent(relativeFrequency -> frequencyAttributes.add(
            new NamedAttribute(
              attributeName + SUFFIX_RELATIVE_FREQUENCY,
              AttributeFactory.getAttribute(String.format(Locale.US, "%.3f", relativeFrequency))
            )));
      }
      Optional<Double> optionalRelativeRank =
        frequencyLookup.getRelativeRank(attributeName, attribute.getAsString());
      if (includeRelativeRank) {
        optionalRelativeRank
          .ifPresent(relativeRank -> frequencyAttributes.add(
            new NamedAttribute(
              attributeName + SUFFIX_RELATIVE_RANK,
              AttributeFactory.getAttribute(String.format(Locale.US, "%.3f", relativeRank))
            )));
      }
      if (includeFrequencyLabel) {
        optionalRelativeRank.ifPresent(relativeRank -> frequencyAttributes.add(
          new NamedAttribute(
            attributeName + SUFFIX_FREQUENCY_LABEL,
            AttributeFactory.getAttribute(String.valueOf(getFrequencyLabel(relativeRank)))
          )));
      }
    }
    return frequencyAttributes;
  }

  private static int getFrequencyLabel(Double relativeRank) {
    if (relativeRank < 0.01) {
      return 0; // Very frequent
    } else if (relativeRank < 0.05) {
      return 1; // Frequent
    } else if (relativeRank < 0.5) {
      return 2; // Medium frequent
    } else if (relativeRank < 0.9) {
      return 3; // Rare
    } else {
      return 4; // Very Rare
    }
  }

  @Override
  public NamedAttribute encodeToSingleAttribute(Record record) {
    List<NamedAttribute> encodedAttributes = encode(record);
    if (encodedAttributes.isEmpty()) {
      throw new IllegalStateException("Encoded record has no attributes.");
    } else if (encodedAttributes.size() > 1) {
      logger.warn("Encoded record has more than one attribute. Returning the first one.");
    }
    return encodedAttributes.getFirst();
  }

  @Override
  public AttributeEncoderGroup<String> addRecordPreprocessor(RecordPreprocessor recordPreprocessor) {
    return this;
  }

  @Override
  public AttributeEncoderGroup<String> addAttributeEncoder(String attributeId,
    AttributeEncoder<?, String> attributeEncoder) {
    return this;
  }

  @Override
  public AttributeEncoderGroup<String> addHardener(Hardener<String> hardener) {
    return this;
  }

  public AttributesFrequencyLookupProvider getFrequencyLookupProvider() {
    return frequencyLookupProvider;
  }

  public List<String> getAttributeNames() {
    return attributeNames;
  }

  public boolean isIncludeRelativeFrequency() {
    return includeRelativeFrequency;
  }

  public void setIncludeRelativeFrequency(boolean includeRelativeFrequency) {
    this.includeRelativeFrequency = includeRelativeFrequency;
  }

  public boolean isIncludeRelativeRank() {
    return includeRelativeRank;
  }

  public void setIncludeRelativeRank(boolean includeRelativeRank) {
    this.includeRelativeRank = includeRelativeRank;
  }

  public boolean isIncludeFrequencyLabel() {
    return includeFrequencyLabel;
  }

  public void setIncludeFrequencyLabel(boolean includeFrequencyLabel) {
    this.includeFrequencyLabel = includeFrequencyLabel;
  }
}

package de.unileipzig.dbs.pprl.core.encoder.attribute;

import de.unileipzig.dbs.pprl.core.common.factories.RecordFactory;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.preprocessing.RecordPreprocessor;
import de.unileipzig.dbs.pprl.core.encoder.crypto.KeyExtractor;
import de.unileipzig.dbs.pprl.core.encoder.hardening.Hardener;
import de.unileipzig.dbs.pprl.core.encoder.model.NamedAttribute;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class SelectivePlainProviderGroup implements AttributeEncoderGroup<String> {

  public static final String ATTRIBUTENAME_SEPARATOR = "#";

  private Set<String> globalAttributeNames;

  private static final Logger logger = LogManager.getLogger(SelectivePlainProviderGroup.class);

  public SelectivePlainProviderGroup(Set<String> globalAttributeNames) {
    this.globalAttributeNames = globalAttributeNames;
  }

  private SelectivePlainProviderGroup() {
  }

  @Override
  public List<NamedAttribute> encode(Record record) {
    List<NamedAttribute> selectedAttributes = new ArrayList<>();

    Optional<String> selectorString = KeyExtractor.extractKey(record);
    String[] attributeNames;
    if (selectorString.isEmpty() || selectorString.get().isEmpty()) {
      if (globalAttributeNames == null || globalAttributeNames.isEmpty()) {
        return new ArrayList<>();
      }
      attributeNames = globalAttributeNames.toArray(new String[0]);
    } else {
      attributeNames = selectorString.get().split(ATTRIBUTENAME_SEPARATOR);
    }
    for (String attributeName : attributeNames) {
      record.getAttribute(attributeName).ifPresent(a -> selectedAttributes.add(new NamedAttribute(attributeName, a)));
    }
    return selectedAttributes;
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

  public Set<String> getGlobalAttributeNames() {
    return globalAttributeNames;
  }

  public void setGlobalAttributeNames(Set<String> globalAttributeNames) {
    this.globalAttributeNames = globalAttributeNames;
  }
}

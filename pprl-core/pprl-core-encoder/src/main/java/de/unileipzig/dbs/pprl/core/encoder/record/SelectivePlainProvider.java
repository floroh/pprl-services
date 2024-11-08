package de.unileipzig.dbs.pprl.core.encoder.record;

import de.unileipzig.dbs.pprl.core.common.factories.RecordFactory;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.encoder.attribute.AttributeEncoderGroup;
import de.unileipzig.dbs.pprl.core.encoder.blocking.BlockingKeyExtractor;
import de.unileipzig.dbs.pprl.core.encoder.crypto.KeyExtractor;

import java.util.Optional;
import java.util.Set;

public class SelectivePlainProvider implements RecordEncoder {

  public static final String ATTRIBUTENAME_SEPARATOR = "#";

  private Set<String> globalAttributeNames;

  @Override
  public Record encode(Record record) {
    Record encodedRecord = RecordFactory.getEmptyRecord(record.getId());
    Optional<String> selectorString = KeyExtractor.extractKey(record);
    String[] attributeNames;
    if (selectorString.isEmpty()) {
      if (globalAttributeNames == null || globalAttributeNames.isEmpty()) {
        return encodedRecord;
      }
      attributeNames = globalAttributeNames.toArray(new String[0]);
    } else {
      attributeNames = selectorString.get().split(ATTRIBUTENAME_SEPARATOR);
    }
    for (String attributeName : attributeNames) {
      record.getAttribute(attributeName).ifPresent(a -> encodedRecord.setAttribute(attributeName, a));
    }
    return encodedRecord;
  }

  @Override
  public RecordEncoder addBlockingKeyExtractor(BlockingKeyExtractor blockingKeyExtractor) {
    return this;
  }

  @Override
  public RecordEncoder addAttributeEncoderGroup(AttributeEncoderGroup attributeEncoderGroup) {
    return this;
  }

  public Set<String> getGlobalAttributeNames() {
    return globalAttributeNames;
  }

  public void setGlobalAttributeNames(Set<String> globalAttributeNames) {
    this.globalAttributeNames = globalAttributeNames;
  }
}

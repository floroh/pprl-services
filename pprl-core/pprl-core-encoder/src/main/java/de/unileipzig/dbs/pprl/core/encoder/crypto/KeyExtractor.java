package de.unileipzig.dbs.pprl.core.encoder.crypto;

import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;

import java.util.Optional;

/**
 * Extracts key/encoding secret from a record.
 */
public class KeyExtractor {

  public static final String KEY_ATTRIBUTE_NAME = "RECORD_KEY";

  public static Optional<String> extractKey(Record record) {
    Optional<Attribute> attribute = record.getAttribute(KEY_ATTRIBUTE_NAME);
//    if (!attribute.isPresent()) {
//      throw new RuntimeException("Missing key attribute: " + KEY_ATTRIBUTE_NAME);
//    }
    return attribute.map(Attribute::getAsString);
  }

}

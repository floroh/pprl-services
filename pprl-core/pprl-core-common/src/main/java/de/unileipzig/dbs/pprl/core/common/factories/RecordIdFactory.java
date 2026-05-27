package de.unileipzig.dbs.pprl.core.common.factories;

import de.unileipzig.dbs.pprl.core.common.model.api.RecordId;
import de.unileipzig.dbs.pprl.core.common.model.impl.RecordIdComposed;
import de.unileipzig.dbs.pprl.core.common.model.impl.RecordIdMap;

public class RecordIdFactory {
  public enum RecordIdVariant {SIMPLE, COMPOSED}

  public static RecordId get(String value) {
    return get(RecordIdVariant.SIMPLE, value);
  }

  public static RecordId get(RecordIdVariant variant, String value) {
    return switch (variant) {
      default -> new RecordIdMap(value);
      case COMPOSED -> RecordIdComposed.ofComposed(value);
    };
  }
}

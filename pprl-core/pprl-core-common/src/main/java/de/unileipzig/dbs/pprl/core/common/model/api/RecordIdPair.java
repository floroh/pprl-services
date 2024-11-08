package de.unileipzig.dbs.pprl.core.common.model.api;

import de.unileipzig.dbs.pprl.core.common.RecordUtils;

public interface RecordIdPair {

  RecordId getLeftRecordId();

  RecordId getRightRecordId();

  default String getPairId() {
    String uniqueIdLeft = getLeftRecordId().getUniqueId();
    String uniqueIdRight = getRightRecordId().getUniqueId();
    return RecordUtils.getPairId(uniqueIdLeft, uniqueIdRight);
  }

}

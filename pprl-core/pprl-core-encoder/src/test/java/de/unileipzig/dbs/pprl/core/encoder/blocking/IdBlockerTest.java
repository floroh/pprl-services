package de.unileipzig.dbs.pprl.core.encoder.blocking;

import de.unileipzig.dbs.pprl.core.common.factories.RecordFactory;
import de.unileipzig.dbs.pprl.core.common.factories.RecordIdFactory;
import de.unileipzig.dbs.pprl.core.common.model.api.BlockingKey;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordId;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class IdBlockerTest {

  @Test
  void extract() {
    String idName = "BLOCK_ID";
    String idValue = "block1234";
    String blockerId = "blkId";

    // Create test record
    RecordId id = RecordIdFactory.get(RecordIdFactory.RecordIdVariant.SIMPLE, "ID0");
    id.addId(idName, idValue);
    id.addId(RecordId.GLOBAL_ID, "abc");
    Record record = RecordFactory.getEmptyRecord(id);

    // Extract blocking keys
    IdBlocker idBlocker = new IdBlocker(blockerId, idName);
    Set<BlockingKey> keys = idBlocker.extract(record);
    assertEquals(1, keys.size());
    BlockingKey idBlk = keys.stream().findFirst().get();
    assertEquals(blockerId, idBlk.getId());
    assertEquals(idValue, idBlk.getValue());
  }
}
package de.unileipzig.dbs.pprl.core.common.model;

import de.unileipzig.dbs.pprl.core.common.model.api.RecordId;
import de.unileipzig.dbs.pprl.core.common.model.impl.RecordIdComposed;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RecordIdComposedTest {

  @Test
  void ofComposedString() {
    String string = "rec-123-org";
    RecordIdComposed id = RecordIdComposed.ofComposed(string);
    assertEquals("123", id.getLocalId());
    assertEquals("org", id.getSourceId());
    assertEquals(string, id.getUniqueId());
    assertThrows(RuntimeException.class, () -> id.getId(RecordId.GLOBAL_ID));
  }

  @Test
  void ofComposedStringWithGlobalId() {
    String string = "rec-123-org-A";
    RecordIdComposed id = RecordIdComposed.ofComposed(string);
    assertEquals("123", id.getLocalId());
    assertEquals("org", id.getSourceId());
    assertEquals(string, id.getUniqueId());
    assertEquals("A", id.getId(RecordId.GLOBAL_ID));
  }
}
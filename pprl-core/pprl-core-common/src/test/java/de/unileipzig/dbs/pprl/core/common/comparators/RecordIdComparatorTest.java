package de.unileipzig.dbs.pprl.core.common.comparators;

import de.unileipzig.dbs.pprl.core.common.factories.RecordIdFactory;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordId;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecordIdComparatorTest {

  @Test
  void compare() {
    RecordId id0 = getId("1", "org");
    RecordId id1 = getId("1", "dup");
    RecordId id2 = getId("1", "org");
    assertNotEquals(id0, id1);
    assertEquals(id0, id2);
  }
  @Test
  void sortComposedId() {
    final List<RecordId> ids = new ArrayList<>();
    ids.add(getId("10", "org"));
    ids.add(getId("1", "org"));
    ids.add(getId("2", "dup"));
    ids.add(getId("0", "dup"));
    ids.add(getId("0", "org"));
    ids.add(getId("1", "dup"));
    ids.add(getId("3", "B"));
    ids.add(getId("3", "A"));
    ids.add(RecordIdFactory.get("zB3"));
    ids.add(RecordIdFactory.get("zA4"));

    ids.sort(new RecordIdComparator());

    System.out.println(ids);
    assertEquals("rec-0-org", ids.get(0).getUniqueId());
    assertEquals("rec-0-dup", ids.get(1).getUniqueId());
    assertEquals("rec-1-org", ids.get(2).getUniqueId());
    assertEquals("rec-1-dup", ids.get(3).getUniqueId());
    assertEquals("rec-2-dup", ids.get(4).getUniqueId());
    assertEquals("rec-3-A", ids.get(5).getUniqueId());
    assertEquals("rec-3-B", ids.get(6).getUniqueId());
    assertEquals("rec-10-org", ids.get(7).getUniqueId());
    assertEquals("zA4", ids.get(8).getLocalId());
    assertEquals("zB3", ids.get(9).getLocalId());
  }

  private RecordId getId(String local, String source) {
    final RecordId id = RecordIdFactory.get(local);
    id.addId(RecordId.SOURCE_ID, source);
    return id;
  }
}
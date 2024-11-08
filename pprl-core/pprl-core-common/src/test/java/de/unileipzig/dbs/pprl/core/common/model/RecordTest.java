package de.unileipzig.dbs.pprl.core.common.model;

import de.unileipzig.dbs.pprl.core.common.factories.AttributeFactory;
import de.unileipzig.dbs.pprl.core.common.factories.RecordFactory;
import de.unileipzig.dbs.pprl.core.common.factories.RecordIdFactory;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecordTest {

  private List<Record> records;

  @BeforeEach
  void setUp() {
    records = new ArrayList<>();
    records.add(initRecord(RecordFactory.RecordVariant.DEFAULT));
    records.add(initRecord(RecordFactory.RecordVariant.LIGHT));
  }

  private Record initRecord(RecordFactory.RecordVariant variant) {
    Record record = RecordFactory.getEmptyRecord(RecordIdFactory.get("123"));
    record.setAttribute("FN", AttributeFactory.getAttribute("Peter"));
    record.setAttribute("LN", AttributeFactory.getAttribute("Müller"));
    return record;
  }

  @Test
  void add() {
    for (Record record : records) {
      record.setAttribute("NEW", AttributeFactory.getAttribute("val"));
      assertTrue(record.getAttribute("NEW").isPresent());
    }
  }

  @Test
  void removeMissingAttribute() {
    for (Record record : records) {
      assertTrue(record.getAttribute("FN").isPresent());
      Record out = record.removeAttribute("FN");
      assertFalse(out.getAttribute("FN").isPresent());
    }
  }

  @Test
  void removeAllAttributes() {
    for (Record record : records) {
      assertFalse(record.getAttributeNames().isEmpty());
      record.removeAllAttributes();
      assertEquals(0, record.getAttributeNames().size());
    }
  }
}
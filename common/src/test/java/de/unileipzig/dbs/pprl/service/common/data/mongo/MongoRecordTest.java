package de.unileipzig.dbs.pprl.service.common.data.mongo;

import de.unileipzig.dbs.pprl.core.common.factories.AttributeFactory;
import de.unileipzig.dbs.pprl.core.common.factories.RecordIdFactory;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MongoRecordTest {

  private List<Record> records;

  @BeforeEach
  void setUp() {
    records = new ArrayList<>();
    records.add(initRecord());
  }

  private Record initRecord() {
    Record record = new MongoRecord(1, RecordIdFactory.get("123"));
    record.setAttribute("FN", AttributeFactory.getAttribute("Peter"));
    record.setAttribute("LN", AttributeFactory.getAttribute("Müller"));
    return record;
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
package de.unileipzig.dbs.pprl.core.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import de.unileipzig.dbs.pprl.core.common.model.impl.SerializableTable;
import org.junit.jupiter.api.Test;
import tech.tablesaw.api.BooleanColumn;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.IntColumn;
import tech.tablesaw.api.LongColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TableSerializationTest {

  public static final Table TABLE = Table.create(
    "tabName",
    StringColumn.create("sCol0", List.of("A,d", "'B", "\"C\"")),
    StringColumn.create("sCol1", List.of("d", "", "f")),
    DoubleColumn.create("dCol0", List.of(1.1, -5.0, 5E-3)),
    LongColumn.create("lCol0", 0L, 1234567890123456L, -975318642L),
    BooleanColumn.create("bCol0", List.of(true, false, true)),
    IntColumn.create("iCol0", 1, -20, 223)
  );

  @Test
  void multiTypeTable() throws JsonProcessingException {
//    System.out.println(TABLE.printAll());

    SerializableTable serializableTable = TableSerialization.toDefaultSerializableTable(TABLE);
//    System.out.println(serializableTable);
    Table table2 = TableSerialization.fromDefaultSerializableTable(serializableTable);
//    System.out.println(table2.printAll());
    assertEquals(TABLE.printAll(), table2.printAll());

    ObjectMapper om = new JsonMapper();
    String jsonString = om.writerWithDefaultPrettyPrinter().writeValueAsString(serializableTable);
//    System.out.println(jsonString);

    SerializableTable clonedSerializableTable = om.readValue(jsonString, SerializableTable.class);
    Table clonedTable = TableSerialization.fromDefaultSerializableTable(clonedSerializableTable);
    assertEquals(TABLE.printAll(), clonedTable.printAll());
  }
}
package de.unileipzig.dbs.pprl.core.common;

import de.unileipzig.dbs.pprl.core.common.model.impl.SerializableTable;
import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;
import tech.tablesaw.columns.numbers.NumberColumnFormatter;
import tech.tablesaw.io.string.DataFramePrinter;
import tech.tablesaw.table.Relation;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

/**
 * Utility class of convertingg Tablesaw {@link Table} objects to and from {@link SerializableTable}.
 * Based on copied code of Tablesaw {@link DataFramePrinter} (under Apache 2 license).
 */
public class TableSerialization {

  public static final int DEFAULT_MAX_ROWS = 1000000;

  private static final String TOO_SHORT_COLUMN_MARKER = "?";

  public static final TableSerialization instance = new TableSerialization();

  private int maxRows;

  public TableSerialization() {
    this(DEFAULT_MAX_ROWS);
  }

  public TableSerialization(int maxRows) {
    this.maxRows = maxRows;
  }

  public static SerializableTable toDefaultSerializableTable(Table table) {
    return instance.toSerializableTable(table);
  }
  public static Table fromDefaultSerializableTable(SerializableTable serializableTable) {
    return instance.fromSerializableTable(serializableTable);
  }

  public SerializableTable toSerializableTable(Table table) {
    table = table.copy();
    Arrays.stream(table.numberColumns()).forEach(c -> c.setPrintFormatter(floatingPointUS_EN()));
    return SerializableTable.builder()
      .name(table.name())
      .types(getColumnTypes(table))
      .header(getHeaderTokens(table))
      .data(getDataTokens(table))
      .build();
  }

  public Table fromSerializableTable(SerializableTable serializableTable) {
    Table table = Table.create(serializableTable.getName());
    List<Column<?>> columns = new ArrayList<>();
    for (int i = 0; i < serializableTable.getHeader().length; i++) {
      ColumnType columnType = ColumnType.valueOf(serializableTable.getTypes()[i]);
      Column<?> column = columnType.create(serializableTable.getHeader()[i]);
      columns.add(column);
    }
    for (int r = 0; r < serializableTable.getData().length; r++) {
      for (int c = 0; c < serializableTable.getData()[r].length; c++) {
        columns.get(c).appendCell(serializableTable.getData()[r][c]);
      }
    }
    return table.addColumns(columns.toArray(new Column[0]));
  }

  public static NumberColumnFormatter floatingPointUS_EN() {
    NumberFormat format =
      new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.US));
    format.setMaximumFractionDigits(340);
    format.setMaximumIntegerDigits(340);
    format.setGroupingUsed(false);
    return new NumberColumnFormatter(format);
  }

  private String[] getColumnTypes(Relation frame) {
    final int colCount = frame.columnCount();
    final String[] types = new String[colCount];
    IntStream.range(0, colCount)
      .forEach(
        colIndex -> {
          types[colIndex] = frame.column(colIndex).type().name();
        });
    return types;
  }

  /**
   * Returns the header string tokens for the frame
   *
   * @param frame the frame to create header tokens
   * @return the header tokens
   */
  private String[] getHeaderTokens(Relation frame) {
    final int colCount = frame.columnCount();
    final String[] header = new String[colCount];
    IntStream.range(0, colCount)
      .forEach(
        colIndex -> {
          header[colIndex] = frame.column(colIndex).name();
        });
    return header;
  }

  private String getDataToken(Column<?> col, int i) {
    return col.size() > i ? col.getString(i) : TOO_SHORT_COLUMN_MARKER;
  }

  /**
   * Returns the 2-D array of data tokens from the frame specified
   *
   * @param frame the DataFrame from which to create 2D array of formatted tokens
   * @return the array of data tokens
   */
  private String[][] getDataTokens(Relation frame) {
    if (frame.rowCount() == 0) {
      return new String[0][0];
    }
    final int rowCount = Math.min(maxRows, frame.rowCount());
    final boolean truncated = frame.rowCount() > maxRows;
    final int colCount = frame.columnCount();
    final String[][] data;
    if (truncated) {
      data = new String[rowCount + 1][colCount];
      int i;
      for (i = 0; i < Math.ceil((double) rowCount / 2); i++) {
        for (int j = 0; j < colCount; j++) {
          Column<?> col = frame.column(j);
          data[i][j] = getDataToken(col, i);
        }
      }
      for (int j = 0; j < colCount; j++) {
        data[i][j] = "...";
      }
      for (++i; i <= rowCount; i++) {
        for (int j = 0; j < colCount; j++) {
          Column<?> col = frame.column(j);
          data[i][j] = getDataToken(col, frame.rowCount() - maxRows + i - 1);
        }
      }
    } else {
      data = new String[rowCount][colCount];
      for (int i = 0; i < rowCount; i++) {
        for (int j = 0; j < colCount; j++) {
          Column<?> col = frame.column(j);
          String value = getDataToken(col, i);
          data[i][j] = value == null ? "" : value;
        }
      }
    }
    return data;
  }
}

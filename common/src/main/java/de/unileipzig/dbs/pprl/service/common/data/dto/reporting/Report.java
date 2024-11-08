package de.unileipzig.dbs.pprl.service.common.data.dto.reporting;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.unileipzig.dbs.pprl.core.common.model.impl.SerializableTable;
import de.unileipzig.dbs.pprl.core.common.TableSerialization;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.tablesaw.api.Table;

import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Report {

  /**
   * Name of this report
   */
  private String name;

  /**
   * Type of this report (see {@link ReportType})
   */
  private ReportType type;

  /**
   * Content of the report, e.g. text or table description
   */
  private String report;

  /**
   * Table content of the report
   */
  private SerializableTable table;

  public Optional<Table> parseTable() {
    if (type != ReportType.TABLE) {
      return Optional.empty();
    }
    return Optional.of(TableSerialization.fromDefaultSerializableTable(table));
  }

  public static Report createTableReport(Table table) {
    return createTableReport(table.name(), table);
  }

  public static Report createTableReport(String name, Table table) {
    return Report.builder()
      .name(name)
      .type(ReportType.TABLE)
      .table(TableSerialization.toDefaultSerializableTable(table))
      .build();
  }

}

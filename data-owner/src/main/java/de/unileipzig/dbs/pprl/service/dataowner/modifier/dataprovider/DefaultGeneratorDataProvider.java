package de.unileipzig.dbs.pprl.service.dataowner.modifier.dataprovider;

import de.unileipzig.dbs.pprl.core.common.model.impl.PersonalAttributeType;
import tech.tablesaw.aggregate.AggregateFunctions;
import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvReadOptions;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides data based on CSV files shipped with this implementation in the resources
 */
public class DefaultGeneratorDataProvider implements GeneratorDataProvider {

  public static final String NAME_RESSOURCE_LOCATION = "data/names.csv";
  public static final String LOCATION_RESSOURCE_LOCATION = "data/plz_city.csv";

  public static Table namesTable = readTableRelative(NAME_RESSOURCE_LOCATION);
  public static Table addressTable = readTableRelative(LOCATION_RESSOURCE_LOCATION);

  @Override
  public List<String> getAllValues(String attributeName, boolean distinct) {
    return getColumn(attributeName).unique().asList();
  }

  @Override
  public List<String> getFrequencyFilteredValues(String attributeName, boolean isRare, double share,
    boolean distinct) {
    StringColumn column = getColumn(attributeName);
    List<String> values = rareOrCommonValues(Table.create(column), column.name(), isRare, share);
    if (distinct) {
      return values.stream().distinct().collect(Collectors.toList());
    }
    return values;
  }


  private static Table readTableRelative(String relativePath) {
    URL url = DefaultGeneratorDataProvider.class.getClassLoader().getResource(relativePath);
    final String location = url.getFile();
    return readTable(location);
  }

  private static Table readTable(String absolutePath) {
    Table table = null;
      table = Table.read().usingOptions(
        CsvReadOptions.builder(absolutePath)
          .columnTypesToDetect(Collections.singletonList(ColumnType.STRING))
      );
      return table;
  }

  private StringColumn getColumn(String attributeName) {
    PersonalAttributeType type = PersonalAttributeType.valueOf(attributeName);
    return switch (type) {
      case FIRSTNAME, LASTNAME -> namesTable.stringColumn(attributeName);
      case PLZ, CITY -> addressTable.stringColumn(attributeName);
      default -> throw new RuntimeException("No data available for attribute: " + attributeName);
    };
  }

  private static List<String> rareOrCommonValues(Table input, String columnName, boolean isRare,
    double share) {
    Table aggregated = input.summarize(columnName, AggregateFunctions.count)
      .by(columnName)
      .sortOn(1);
//    System.out.println(aggregated.print());
    StringColumn sortedValueColumn = aggregated.stringColumn(0);
    int totalSize = aggregated.rowCount();
    List<String> selectedValues = isRare ?
      sortedValueColumn.first((int) (share * totalSize)).asList()
      : sortedValueColumn.last((int) (share * totalSize)).asList();
    return selectedValues;
  }
}

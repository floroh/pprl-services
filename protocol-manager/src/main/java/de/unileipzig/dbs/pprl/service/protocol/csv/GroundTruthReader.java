package de.unileipzig.dbs.pprl.service.protocol.csv;

import de.unileipzig.dbs.pprl.core.matcher.evaluation.GroundTruth;
import lombok.extern.slf4j.Slf4j;
import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvReadOptions;

import java.io.File;
import java.util.Optional;

@Slf4j
public class GroundTruthReader {

  private final String dataSetPath;

  public GroundTruthReader(String dataSetPath) {
    this.dataSetPath = dataSetPath;
  }

  public Optional<GroundTruth> readGroundTruth() {
    return readGroundTruth(dataSetPath + File.separator + "groundTruth" +
      File.separator + "links.csv");
  }

  public static Optional<GroundTruth> readGroundTruth(String path) {
    try {
      Table table = Table.read().usingOptions(
        CsvReadOptions.builder(path)
          .columnTypes(new ColumnType[] {ColumnType.STRING, ColumnType.STRING})
      );
      return Optional.of(GroundTruth.createFromLinksTable(table));
    } catch (IllegalStateException e) {
      log.info("Could not read ground truth");
      return Optional.empty();
    }
  }
}

package de.unileipzig.dbs.pprl.service.protocol.csv;

import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import de.unileipzig.dbs.pprl.service.common.csv.CsvImporter;
import de.unileipzig.dbs.pprl.service.common.csv.DatasetCsvSchema;
import de.unileipzig.dbs.pprl.service.common.csv.PersonRecord;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordDto;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

public class CsvReader {

  public static List<RecordDto> readRecords(String path) throws IOException {
    CsvImporter importer = new CsvImporter(path + File.separator + "records.csv");
    if (Files.exists(new File(path + File.separator + "schema.json").toPath())) {
      importer.setSchema(DatasetCsvSchema.read(path + File.separator + "schema.json").toCsvSchema());
    } else {
      importer.setSchema(
        CsvSchema.builder()
          .setUseHeader(true)
          .build()
      );
    }
    List<PersonRecord> csvRecords = importer.getRecords();
    List<RecordDto> plainRecords = csvRecords.stream()
      .map(PersonRecord::toRecordDto)
      .collect(Collectors.toList());
    return plainRecords;
  }
}

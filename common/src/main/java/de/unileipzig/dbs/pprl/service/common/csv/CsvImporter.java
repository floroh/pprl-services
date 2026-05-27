package de.unileipzig.dbs.pprl.service.common.csv;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordDto;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CsvImporter {

  private String path;

  private CsvSchema schema;

  public CsvImporter(String path) {
    this.path = path;
  }


  public List<RecordDto> getRecordDtos() throws IOException {
    File input = new File(path);
    CsvMapper mapper = new CsvMapper();

    MappingIterator<Map<String,String>> it =
            mapper
                    .readerFor(Map.class)
                    .with(schema)
                    .readValues(input);

    List<RecordDto> result = new ArrayList<>();
    CsvRowConverter converter = new CsvRowConverter();

    while (it.hasNext()) {
      Map<String, String> row = it.next();
      RecordDto dto = converter.rowToRecordDto(row);
      if (dto != null) {
        result.add(dto);
      }
    }
    return result;
  }

  public List<PersonRecord> getRecords() throws IOException {
    return getIterator().readAll();
  }

  public MappingIterator<PersonRecord> getIterator() throws IOException {
    File input = new File(path);
    CsvMapper mapper = getMapper();

    if (schema == null) {
      schema = mapper.schemaFor(PersonRecord.class);
    }

    MappingIterator<PersonRecord> it =
      mapper.readerWithSchemaFor(PersonRecord.class).with(schema).readValues(input);
    return it;
  }

  public void setSchema(CsvSchema schema) {
    this.schema = schema;
  }

  protected static CsvMapper getMapper() {
    return CsvMapper.builder()
      .configure(CsvParser.Feature.EMPTY_STRING_AS_NULL, true)
      .configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true)
      .build();
  }
}

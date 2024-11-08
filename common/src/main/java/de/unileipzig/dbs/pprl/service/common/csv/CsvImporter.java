package de.unileipzig.dbs.pprl.service.common.csv;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class CsvImporter {

  private String path;

  private CsvSchema schema;

  public CsvImporter(String path) {
    this.path = path;
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

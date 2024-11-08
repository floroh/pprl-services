package de.unileipzig.dbs.pprl.service.common.csv;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DatasetCsvSchema {

  private boolean header = false;
  private String charset = "UTF-8";
  private String format = "RFC4180";

  @Singular
  private List<CsvColumn> columns;

  public CsvSchema toCsvSchema() {
    CsvSchema.Builder builder = CsvSchema.builder();
    if (header) {
      builder.setUseHeader(true);
    }
    for (CsvColumn column : columns) {
      // TODO: add support for other types
      builder.addColumn(column.getName(), CsvSchema.ColumnType.STRING);
    }
    return builder.build();
  }

  public static DatasetCsvSchema read(String path) throws IOException {
    return new ObjectMapper().readValue(Path.of(path).toFile(), DatasetCsvSchema.class);
  }

  public void write(String path) throws IOException {
    write(this, path);
  }

  public static void write(DatasetCsvSchema schema, String path) throws IOException {
    String serialized = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(schema);
    FileOutputStream fileOutputStream = new FileOutputStream(path, false);
    FileChannel fileChannel = fileOutputStream.getChannel();
    ByteBuffer byteBuffer = ByteBuffer.wrap(serialized.getBytes(StandardCharsets.UTF_8));
    fileChannel.write(byteBuffer);
  }

  public static void main(String[] args) {
    DatasetCsvSchema schema = DatasetCsvSchema.builder()
      .header(false)
      .charset("UTF-8")
      .format("RFC4180")
      .column(CsvColumn.builder().name("id.source").build())
      .column(CsvColumn.builder().name("id.global").build())
      .column(CsvColumn.builder().name("id.local").build())
      .column(CsvColumn.builder().name("firstname").build())
      .column(CsvColumn.builder().name("middleName").build())
      .column(CsvColumn.builder().name("lastName").build())
      .column(CsvColumn.builder().name("yearOfBirth").build())
      .column(CsvColumn.builder().name("placeOfBirth").build())
      .column(CsvColumn.builder().name("country").build())
      .column(CsvColumn.builder().name("city").build())
      .column(CsvColumn.builder().name("plz").build())
      .column(CsvColumn.builder().name("street").build())
      .column(CsvColumn.builder().name("gender").build())
      .column(CsvColumn.builder().name("IGNORE").build())
      .column(CsvColumn.builder().name("IGNORE_2").build())
      .build();
    try {
      schema.write("/tmp/ncvr-gen-schema.json");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}

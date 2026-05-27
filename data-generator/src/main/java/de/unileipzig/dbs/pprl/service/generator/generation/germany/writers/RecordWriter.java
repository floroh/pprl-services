package de.unileipzig.dbs.pprl.service.generator.generation.germany.writers;

import de.unileipzig.dbs.pprl.service.generator.generation.germany.records.Record;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class RecordWriter {

  public RecordWriter() {
  }

  public void writeRecordToCsvFile(
          Record record,
          int requestedNumberOfRecords,
          boolean headerIncluded,
          String destinationFolder,
          String fileName) throws IOException {

    try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(destinationFolder + fileName + ".csv"))) {
      if (headerIncluded) {
        bufferedWriter.write(record.attributeNamesToString());
        bufferedWriter.newLine();
      }
      for (int i = 1; i <= requestedNumberOfRecords; i++) {
        record.nextValues();
        bufferedWriter.write(record.attributeValuesToString());
        bufferedWriter.newLine();
      }
    }
  }
}
package de.unileipzig.dbs.pprl.service.protocol.scripts;

import de.unileipzig.dbs.pprl.service.common.data.dto.DatasetDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.GroundTruthDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordIdDto;
import de.unileipzig.dbs.pprl.service.protocol.api.EncoderApi;
import de.unileipzig.dbs.pprl.service.protocol.csv.CsvReader;
import de.unileipzig.dbs.pprl.service.protocol.csv.JacksonObjectMapper;
import kong.unirest.Unirest;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class RecordInserter {

  private String serviceUrl;

  public RecordInserter(String serviceUrl) {
    this();
    this.serviceUrl = serviceUrl;
  }

  public RecordInserter() {
    Unirest.config()
      .addDefaultHeader("Content-Type", "application/json")
      .setObjectMapper(new JacksonObjectMapper());
  }

  public RecordIdDto insert(RecordDto recordIn) {
    return Unirest.post(serviceUrl + "/record")
      .body(recordIn)
      .asObject(RecordIdDto.class)
      .getBody();
  }

  public void delete(int datasetId) {
    String endPoint = serviceUrl + "/record/" + datasetId + "/all";
    Unirest.delete(endPoint)
      .asString();
  }

  public List<RecordIdDto> batchInsert(List<RecordDto> recordsIn) {
    RecordIdDto[] recordIds = Unirest.post(serviceUrl + "/record/batch")
      .body(recordsIn)
      .asObject(RecordIdDto[].class)
      .getBody();
    return Arrays.asList(recordIds);
  }

  public void addGroundTruth(GroundTruthDto groundTruthDto) {
    Unirest.post(serviceUrl + "/evaluation/ground-truth")
      .body(groundTruthDto)
      .asEmpty();
  }

  public GroundTruthDto getGroundTruth(int datasetId) {
    return Unirest.get(serviceUrl + "/evaluation/ground-truth/" + datasetId)
      .asObject(GroundTruthDto.class)
      .getBody();
  }

  public DatasetDto addDatasetDescription(DatasetDto datasetDto) {
    return Unirest.post(serviceUrl + "/record/datasets")
      .body(datasetDto)
      .asObject(DatasetDto.class)
      .getBody();
  }

  private static void insertDataset(String path, int datasetId) throws IOException {
    RecordInserter inserter = new RecordInserter(EncoderApi.encoderUrl);
    inserter.insertDatasetFromCsv(path, datasetId);
  }

  public void insertDatasetFromCsv(String path, int datasetId) {
    log.info("Importing records from {} ", path);

    delete(datasetId);
    List<RecordDto> plainRecords = null;
    try {
      plainRecords = CsvReader.readRecords(path).stream()
        .peek(record -> record.setDatasetId(datasetId))
        .collect(Collectors.toList());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    log.debug("first record: " + plainRecords.getFirst());

    log.info("Batch inserting records...");
    long start = System.currentTimeMillis();
    List<RecordIdDto> recordIdDtos = batchInsert(plainRecords);
    long end = System.currentTimeMillis();
    log.info("Batch inserted {} records in {} ms", recordIdDtos.size(), end - start);
  }

}

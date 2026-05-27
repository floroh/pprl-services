package de.unileipzig.dbs.pprl.service.protocol.api;

import de.unileipzig.dbs.pprl.core.common.monitoring.Tag;
import de.unileipzig.dbs.pprl.service.common.data.dto.*;
import de.unileipzig.dbs.pprl.service.common.data.dto.analysis.AnalysisRequestDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.analysis.AnalysisResultDto;
import de.unileipzig.dbs.pprl.service.protocol.csv.CsvReader;
import de.unileipzig.dbs.pprl.service.protocol.csv.JacksonObjectMapper;
import de.unileipzig.dbs.pprl.service.protocol.model.dto.DatasetCsvDto;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class CommonApi {

  public String url = "http://localhost:8081";

  public CommonApi(String url) {
    this();
    this.url = url;
  }

  public CommonApi() {
    Unirest.config().reset();
    Unirest.config()
            .addDefaultHeader("Content-Type", "application/json")
            .socketTimeout(3_600_000)  // 60min
            .setObjectMapper(new JacksonObjectMapper());
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public boolean getHealth() {
    String endPoint = url + "/actuator/health";
    try {
      HttpResponse<String> response = Unirest.get(endPoint)
              .asString();
      log.info("Health of " + url + ": " + response.getBody());
      return response.isSuccess();
    } catch (Exception e) {
      log.error("Health of " + url + ": failed with " + e.getMessage());
      return false;
    }
  }

  public RecordIdDto insert(RecordDto recordIn) {
    return Unirest.post(url + "/record")
            .body(recordIn)
            .asObject(RecordIdDto.class)
            .getBody();
  }

  public void delete(long datasetId) {
    String endPoint = url + "/record/" + datasetId + "/all";
    Unirest.delete(endPoint)
            .asString();
  }

  public List<RecordIdDto> batchInsert(List<RecordDto> recordsIn) {
    final int BATCH_SIZE = 100000; // configurable batch size
    List<RecordIdDto> allRecordIds = new ArrayList<>();
    if (recordsIn.size() > BATCH_SIZE) {
      log.info("Using chunked batch insert of size {} for {} records.", BATCH_SIZE, recordsIn.size());
    }
    for (int i = 0; i < recordsIn.size(); i += BATCH_SIZE) {
      int end = Math.min(i + BATCH_SIZE, recordsIn.size());
      List<RecordDto> batch = recordsIn.subList(i, end);
      RecordIdDto[] recordIds = Unirest.post(url + "/record/batch")
              .body(batch)
              .asObject(RecordIdDto[].class)
              .getBody();
      if (recordIds != null) {
        allRecordIds.addAll(Arrays.asList(recordIds));
      }
    }
    return allRecordIds;
  }

  public void addGroundTruth(GroundTruthDto groundTruthDto) {
    Unirest.post(url + "/evaluation/ground-truth")
            .body(groundTruthDto)
            .asEmpty();
  }

  public GroundTruthDto getGroundTruth(long datasetId) {
    return Unirest.get(url + "/evaluation/ground-truth/" + datasetId)
            .asObject(GroundTruthDto.class)
            .getBody();
  }

  public void insertDatasetFromCsv(DatasetCsvDto datasetCsvDto) {
    log.info("Importing records with config {} ", datasetCsvDto);

    DatasetDto datasetDto = datasetCsvDto.getDatasetDto();
    delete(datasetDto.getDatasetId());
    List<RecordDto> plainRecords = null;
    try {
      plainRecords = CsvReader.readRecords(datasetCsvDto.getPath()).stream()
              .peek(record -> record.setDatasetId(datasetDto.getDatasetId()))
              .collect(Collectors.toList());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    log.debug("First record: " + plainRecords.getFirst());

    addDatasetDescription(datasetDto);
    log.info("Batch inserting records...");
    long start = System.currentTimeMillis();
    List<RecordIdDto> recordIdDtos = batchInsert(plainRecords);
    long end = System.currentTimeMillis();
    log.info("Batch inserted {} records in {} ms", recordIdDtos.size(), end - start);
  }

  public void deleteDataset(long datasetId) {
    String endPoint = url + "/datasets/" + datasetId;
    Unirest.delete(endPoint).asEmpty();
  }

  public DatasetDto addDatasetDescription(DatasetDto datasetDto) {
    return Unirest.post(url + "/record/datasets")
            .body(datasetDto)
            .asObject(DatasetDto.class)
            .getBody();
  }

  public Optional<DatasetDto> getDatasetDescription(long datasetId) {
    String endPoint = url + "/record/datasets/" + datasetId;
    HttpResponse<DatasetDto> response = Unirest.get(endPoint)
            .asObject(DatasetDto.class);
    if (response.getStatus() == 200) {
      return Optional.ofNullable(response.getBody());
    } else {
      return Optional.empty();
    }
  }

  public List<DatasetDto> getDatasetDescriptions() {
    String endPoint = url + "/record/datasets";
    DatasetDto[] datasetDtos = Unirest.get(endPoint)
            .asObject(DatasetDto[].class)
            .getBody();
    return Arrays.asList(datasetDtos);
  }

  public void addTags(long datasetId, List<Tag> tags) {
    final int BATCH_SIZE = 100000; // configurable batch size
    String endPoint = url + "/analysis/tag/" + datasetId;

    if (tags.size() > BATCH_SIZE) {
      log.info("Using chunked batch insert of size {} for {} tags.", BATCH_SIZE, tags.size());
    }
    for (int i = 0; i < tags.size(); i += BATCH_SIZE) {
      int end = Math.min(i + BATCH_SIZE, tags.size());
      List<Tag> batch = tags.subList(i, end);
      Unirest.post(endPoint)
              .body(batch)
              .asEmpty();
    }
  }

  public List<Tag> getTags(long datasetId, String origin) {
    String endPoint = url + "/analysis/tag/" + datasetId + "/" + origin;
    if (origin == null) {
      endPoint = url + "/analysis/tag/" + datasetId;
    }
    Tag[] tags = Unirest.get(endPoint)
            .asObject(Tag[].class)
            .getBody();
    return Arrays.asList(tags);
  }

  public List<Tag> getPairTags(long datasetId, List<RecordIdPairDto> idPairs) {
    String endPoint = url + "/analysis/pair-tags/" + datasetId;
    Tag[] tags = Unirest.post(endPoint)
            .body(idPairs)
            .asObject(Tag[].class)
            .getBody();
    return Arrays.asList(tags);
  }

  public AnalysisResultDto getAnalysisResult(long datasetId, String analysisType) {
    String endPoint = url + "/analysis/run";
    return Unirest.post(endPoint)
            .body(AnalysisRequestDto.builder()
                    .datasetId(datasetId)
                    .type(analysisType)
                    .build()
            )
            .asObject(AnalysisResultDto.class)
            .getBody();
  }

  public List<RecordDto> getRecords(long datasetId) {
    String endPoint = url + "/record/" + datasetId + "/all";
    RecordDto[] records = Unirest.get(endPoint)
            .asObject(RecordDto[].class)
            .getBody();
    return Arrays.asList(records);
  }

}

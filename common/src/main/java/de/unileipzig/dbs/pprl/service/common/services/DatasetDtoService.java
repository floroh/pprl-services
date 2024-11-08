package de.unileipzig.dbs.pprl.service.common.services;

import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.matcher.classification.Classifier;
import de.unileipzig.dbs.pprl.core.matcher.model.api.LinkageProcessDataSet;
import de.unileipzig.dbs.pprl.service.common.data.converter.DatasetConverter;
import de.unileipzig.dbs.pprl.service.common.data.converter.MongoRecordConverter;
import de.unileipzig.dbs.pprl.service.common.data.converter.RecordIdPairConverter;
import de.unileipzig.dbs.pprl.service.common.data.dto.DatasetDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.GroundTruthDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordIdDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordIdPairDto;
import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for accessing datasets based on Dto objects and therefore primarily via controllers
 */
@Service
@Slf4j
public class DatasetDtoService {

  private final DatasetMongoService datasetService;

  private final MongoRecordConverter mongoRecordConverter = new MongoRecordConverter();

  private final MongoTemplate mongoTemplate;

  public DatasetDtoService(
    DatasetMongoService datasetService, MongoTemplate mongoTemplate) {
    this.datasetService = datasetService;
    this.mongoTemplate = mongoTemplate;
  }

  public RecordIdDto insert(RecordDto recordDto) {
    MongoRecord record = (MongoRecord) mongoRecordConverter.toRecord(recordDto);
    record.getProperties().add(LinkageProcessDataSet.NEW);
    datasetService.addRecord(record.getIdDataset(), record);
    RecordIdDto idDto = recordDto.getId();
    RecordIdDto recordIdDto = addUnique(idDto, record);
    log.info("Inserted record with id: " + recordIdDto);
    return recordIdDto;
  }

  public List<RecordIdDto> insertAll(List<RecordDto> recordDtos) {
    log.info("Starting bulk insert of " + recordDtos.size() + " records");
    List<Integer> idDatasets = recordDtos.stream()
      .map(RecordDto::getDatasetId)
      .distinct().collect(Collectors.toList());
    if (idDatasets.isEmpty()) {
      throw new RuntimeException("Records have missing dataset ids");
    } else if (idDatasets.size() > 1) {
      throw new RuntimeException("Bulk insertion of records from different datasets is not supported");
    }
    int idDataset = idDatasets.getFirst();

    List<MongoRecord> records = recordDtos.stream()
      .map(mongoRecordConverter::toRecord)
      .map(r -> (MongoRecord) r)
      .peek(r -> r.getProperties().add(LinkageProcessDataSet.NEW))
      .collect(Collectors.toList());
    log.debug("Finished conversion of " + recordDtos.size() + " records");

    datasetService.addRecords(idDataset, records);

    List<RecordIdDto> idDtos = new ArrayList<>();
    for (int i = 0; i < recordDtos.size(); i++) {
      RecordIdDto idDto = recordDtos.get(i).getId();
      RecordIdDto recordIdDto = addUnique(idDto, records.get(i));
      idDtos.add(recordIdDto);
    }
    log.debug("Finished bulk insert");
    return idDtos;
  }

  public void addGroundTruth(GroundTruthDto groundTruthDto) {
    datasetService.addGroundTruth(RecordIdPairConverter.fromDto(groundTruthDto));
  }

  public Optional<GroundTruthDto> getGroundTruth(int idDataset) {
    return datasetService.getGroundTruth(idDataset).stream().map(RecordIdPairConverter::toDto).findFirst();
  }

  public List<RecordDto> getRecordsWithGlobalIdIfAvailable(int idDataset) {
    List<RecordDto> records = getAllRecordsAsDto(idDataset);
    Optional<GroundTruthDto> optionalGroundTruthDto = getGroundTruth(idDataset);
    if (optionalGroundTruthDto.isPresent()) {
      addGlobalIdsBasedOnGroundTruth(records, optionalGroundTruthDto);
    }
    return records;
  }

  private static void addGlobalIdsBasedOnGroundTruth(List<RecordDto> records,
    Optional<GroundTruthDto> optionalGroundTruthDto) {
    log.info("Adding global ids based on ground truth");
    int i = 0;
    Map<String, String> idToGlobalId = new HashMap<>();
    GroundTruthDto gt = optionalGroundTruthDto.get();
    for (RecordIdPairDto pair : gt.getRecordIdPairs()) {
      idToGlobalId.put(pair.getLeftRecordId().getUniqueLike(), String.valueOf(i));
      if (pair.getLabel().equals(Classifier.Label.TRUE_MATCH)) {
        idToGlobalId.put(pair.getRightRecordId().getUniqueLike(), String.valueOf(i));
      } else {
        i++;
        idToGlobalId.put(pair.getRightRecordId().getUniqueLike(), String.valueOf(i));
      }
      i++;
    }
    for (RecordDto record : records) {
      String globalId = idToGlobalId.get(record.getId().getUniqueLike());
      if (globalId == null) {
        globalId = String.valueOf(i);
        i++;
      }
      record.getId().setGlobal(globalId);
    }
  }

  public RecordIdDto update(RecordDto recordDto) {
    String uniqueId = recordDto.getId().getUnique();
    Optional<MongoRecord> optionalRecord = datasetService.getRecord(uniqueId);
    if (optionalRecord.isPresent()) {
      MongoRecord oldRecord = optionalRecord.get();
      MongoRecord newRecord = (MongoRecord) mongoRecordConverter.toRecord(recordDto);
      for (String attributeName : oldRecord.getAttributeNames()) {
        Optional<Attribute> curAttribute = newRecord.getAttribute(attributeName);
        if (curAttribute.isPresent()) {
          oldRecord.setAttribute(attributeName, curAttribute.get());
        } else {
          oldRecord.removeAttribute(attributeName);
        }
      }
      datasetService.addRecord(recordDto.getDatasetId(), oldRecord);
      log.info("Updated existing record with id: " + oldRecord.getId());
      return recordDto.getId();
    }
    throw new RuntimeException("Update failed for record with id: " + recordDto.getId());
  }

  public RecordDto getRecordAsDto(String uniqueId) {
    Optional<MongoRecord> mongoRecord = datasetService.getRecord(uniqueId);
    if (mongoRecord.isEmpty()) {
      throw new RuntimeException("No mongoRecord found for id: " + uniqueId);
    }
    return mongoRecordConverter.fromRecord(mongoRecord.get());
  }

  public List<RecordDto> getRecordsAsDto(List<String> uniqueIds) {
    log.debug("Retrieving " + uniqueIds.size() + " records from the database");
    return datasetService.getRecords(uniqueIds).stream()
      .map(mongoRecordConverter::fromRecord)
      .collect(Collectors.toList());
  }

  public List<RecordDto> getAllRecordsAsDto(int idDataset) {
    log.debug("Retrieving all records of dataset {} from the database", idDataset);
    return datasetService.getAllRecords(idDataset).stream()
      .map(mongoRecordConverter::fromRecord)
      .collect(Collectors.toList());
  }

  public RecordDto getRecordAsDto(int idDataset, RecordIdDto idDto) {
    return datasetService.getRecord(idDataset, MongoRecordConverter.toRecordId(idDto))
      .map(mongoRecordConverter::fromRecord)
      .orElseThrow(
        () -> new RuntimeException("No record found for id " + idDto + " in dataset " + idDataset));
  }

  public List<RecordDto> getRecordsAsDto(int idDataset, List<RecordIdDto> idDtos) {
    log.debug("Retrieving " + idDtos.size() + " records from the database");
    Query query = new Query();
    List<Criteria> idCriteria = idDtos.stream()
      .map(idDto ->
        Criteria.where("recordId.ids.SOURCE_ID").is(idDto.getSource())
          .andOperator(
            Criteria.where("recordId.ids.LOCAL_ID").is(idDto.getLocal())
          ))
      .collect(Collectors.toList());
    query.addCriteria(
      Criteria.where("idDataset").is(idDataset)
        .andOperator(new Criteria().orOperator(idCriteria))
    );
    return mongoTemplate.find(query, MongoRecord.class).stream()
      .map(mongoRecordConverter::fromRecord)
      .collect(Collectors.toList());
  }

  public List<RecordDto> getRecordsBySourceAsDto(int idDataset, String source) {
    return datasetService.getRecordsBySource(idDataset, source).stream()
      .map(mongoRecordConverter::fromRecord)
      .collect(Collectors.toList());
  }

  public Long getNumberOfRecords(int idDataset) {
    return datasetService.size(idDataset);
  }

  public DatasetDto addDataset(DatasetDto datasetDto) {
    return DatasetConverter.toDto(datasetService.addDataset(DatasetConverter.fromDto(datasetDto)));
  }

  public void deleteDataset(int datasetId) {
    deleteAllRecords(datasetId);
    datasetService.deleteDataset(datasetId);
  }

  public Optional<DatasetDto> getDataset(int idDataset) {
    return datasetService.getDataset(idDataset).map(DatasetConverter::toDto);
  }

  public List<DatasetDto> getDatasets(Optional<Integer> plaintextDatasetId) {
    return datasetService.getDatasets(plaintextDatasetId).stream()
      .map(DatasetConverter::toDto)
      .toList();
  }

  public List<Integer> getDatasetIds() {
    log.debug("Retrieving all dataset ids from the database");
    return datasetService.getDatasetIds();
  }

  public void deleteAllRecords(int idDataset) {
    log.info("Deleting all records of dataset {} from the database", idDataset);
    datasetService.deleteAll(idDataset);
  }

  public Optional<MongoRecord> getRecord(RecordIdDto idDto) {
    return getRecord(idDto.getUnique());
  }

  public Optional<MongoRecord> getRecord(String uniqueId) {
    return datasetService.getRecord(uniqueId);
  }

  private RecordIdDto addUnique(RecordIdDto idDto, MongoRecord mongoRecord) {
    idDto.setUnique(getUniqueIdFromMongoRecord(mongoRecord));
    return idDto;
  }

  private String getUniqueIdFromMongoRecord(MongoRecord mongoRecord) {
    return mongoRecord.getObjectId().toString();
  }
}

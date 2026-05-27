package de.unileipzig.dbs.pprl.service.common.services;

import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.impl.PersonalAttributeType;
import de.unileipzig.dbs.pprl.core.common.preprocessing.DateSplitter;
import de.unileipzig.dbs.pprl.core.matcher.classification.Classifier;
import de.unileipzig.dbs.pprl.core.matcher.model.api.LinkageProcessDataSet;
import de.unileipzig.dbs.pprl.service.common.config.PreprocessingConfig;
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

  private final PreprocessingConfig preprocessingConfig;

  public DatasetDtoService(
          DatasetMongoService datasetService, MongoTemplate mongoTemplate, PreprocessingConfig preprocessingConfig) {
    this.datasetService = datasetService;
    this.mongoTemplate = mongoTemplate;
    this.preprocessingConfig = preprocessingConfig;
  }

  public RecordIdDto insert(RecordDto recordDto) {
    MongoRecord record = (MongoRecord) mongoRecordConverter.toRecord(recordDto);
    record.getProperties().add(LinkageProcessDataSet.NEW);
    record = preprocessRecord(record);
    datasetService.addRecord(record.getDatasetId(), record);
    RecordIdDto idDto = recordDto.getId();
    RecordIdDto recordIdDto = addUnique(idDto, record);
    log.info("Inserted record with id: " + recordIdDto);
    return recordIdDto;
  }

  public List<RecordIdDto> insertAll(List<RecordDto> recordDtos) {
    log.info("Starting bulk insert of " + recordDtos.size() + " records");
    List<Long> datasetIds = recordDtos.stream()
      .map(RecordDto::getDatasetId)
      .distinct().collect(Collectors.toList());
    if (datasetIds.isEmpty()) {
      throw new RuntimeException("Records have missing dataset ids");
    } else if (datasetIds.size() > 1) {
      throw new RuntimeException("Bulk insertion of records from different datasets is not supported");
    }
    long datasetId = datasetIds.getFirst();

    List<MongoRecord> records = recordDtos.stream()
      .map(mongoRecordConverter::toRecord)
      .map(r -> (MongoRecord) r)
      .peek(r -> r.getProperties().add(LinkageProcessDataSet.NEW))
      .map(this::preprocessRecord)
      .collect(Collectors.toList());
    log.debug("Finished conversion of " + recordDtos.size() + " records");

    datasetService.addRecords(datasetId, records);

    List<RecordIdDto> idDtos = new ArrayList<>();
    for (int i = 0; i < recordDtos.size(); i++) {
      RecordIdDto idDto = recordDtos.get(i).getId();
      RecordIdDto recordIdDto = addUnique(idDto, records.get(i));
      idDtos.add(recordIdDto);
    }
    log.debug("Finished bulk insert");
    return idDtos;
  }

  public MongoRecord preprocessRecord(MongoRecord in) {
    if (this.preprocessingConfig.isSplitDateOfBirthAttributeIfPossible()) {
      Optional<Attribute> dob = in.getAttribute(PersonalAttributeType.DATEOFBIRTH.name());
      Optional<Attribute> yob = in.getAttribute(PersonalAttributeType.YEAROFBIRTH.name());
      if (dob.isPresent() && yob.isEmpty()) {
        DateSplitter dateSplitter = new DateSplitter(true);
        dateSplitter.setInPlace(true);
        if (this.preprocessingConfig.getDatePattern() != null && !this.preprocessingConfig.getDatePattern().isBlank()) {
          dateSplitter.setInputDatePattern(this.preprocessingConfig.getDatePattern());
        }
        return (MongoRecord) dateSplitter.preprocess(in);
      }
    }
    return in;
  }

  public void addGroundTruth(GroundTruthDto groundTruthDto) {
    datasetService.addGroundTruth(RecordIdPairConverter.fromDto(groundTruthDto));
  }

  public Optional<GroundTruthDto> getGroundTruth(long datasetId) {
    return datasetService.getGroundTruth(datasetId).stream().map(RecordIdPairConverter::toDto).findFirst();
  }

  public List<RecordDto> getRecordsWithGlobalIdIfAvailable(long datasetId) {
    List<RecordDto> records = getAllRecordsAsDto(datasetId);
    Optional<GroundTruthDto> optionalGroundTruthDto = getGroundTruth(datasetId);
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

  public List<RecordDto> getAllRecordsAsDto(long datasetId) {
    log.debug("Retrieving all records of dataset {} from the database", datasetId);
    return datasetService.getAllRecords(datasetId).stream()
      .map(mongoRecordConverter::fromRecord)
      .collect(Collectors.toList());
  }

  public RecordDto getRecordAsDto(long datasetId, RecordIdDto idDto) {
    return datasetService.getRecord(datasetId, MongoRecordConverter.toRecordId(idDto))
      .map(mongoRecordConverter::fromRecord)
      .orElseThrow(
        () -> new RuntimeException("No record found for id " + idDto + " in dataset " + datasetId));
  }

  public List<RecordDto> getRecordsAsDto(long datasetId, List<RecordIdDto> idDtos) {
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
      Criteria.where("datasetId").is(datasetId)
        .andOperator(new Criteria().orOperator(idCriteria))
    );
    return mongoTemplate.find(query, MongoRecord.class).stream()
      .map(mongoRecordConverter::fromRecord)
      .collect(Collectors.toList());
  }

  public List<RecordDto> getRecordsBySourceAsDto(long datasetId, String source) {
    return datasetService.getRecordsBySource(datasetId, source).stream()
      .map(mongoRecordConverter::fromRecord)
      .collect(Collectors.toList());
  }

  public Long getNumberOfRecords(long datasetId) {
    return datasetService.size(datasetId);
  }

  public DatasetDto addDataset(DatasetDto datasetDto) {
    return DatasetConverter.toDto(datasetService.addDataset(DatasetConverter.fromDto(datasetDto)));
  }

  public void deleteDataset(long datasetId) {
    deleteAllRecords(datasetId);
    datasetService.deleteDataset(datasetId);
  }

  public Optional<DatasetDto> getDataset(long datasetId) {
    return datasetService.getDataset(datasetId).map(DatasetConverter::toDto);
  }

  public List<DatasetDto> getDatasets(Optional<Long> plaintextDatasetId) {
    return datasetService.getDatasets(plaintextDatasetId).stream()
      .map(DatasetConverter::toDto)
      .toList();
  }

  public List<Long> getDatasetIds() {
    log.debug("Retrieving all dataset ids from the database");
    return datasetService.getDatasetIds();
  }

  public void deleteAllRecords(long datasetId) {
    log.info("Deleting all records of dataset {} from the database", datasetId);
    datasetService.deleteAll(datasetId);
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

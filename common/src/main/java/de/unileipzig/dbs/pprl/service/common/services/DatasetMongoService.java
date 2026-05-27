package de.unileipzig.dbs.pprl.service.common.services;

import de.unileipzig.dbs.pprl.core.common.model.api.BlockingKey;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordId;
import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoCluster;
import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoDataset;
import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoGroundTruth;
import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoRecord;
import de.unileipzig.dbs.pprl.service.common.dataset.DatabaseBlockedDataSet;
import de.unileipzig.dbs.pprl.service.common.persistence.repositories.mongo.MongoClusterRepository;
import de.unileipzig.dbs.pprl.service.common.persistence.repositories.mongo.MongoDatasetRepository;
import de.unileipzig.dbs.pprl.service.common.persistence.repositories.mongo.MongoGroundTruthRepository;
import de.unileipzig.dbs.pprl.service.common.persistence.repositories.mongo.MongoRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Service for accessing datasets
 */
@Slf4j
@Service
public class DatasetMongoService {

  private final MongoTemplate mongoTemplate;

  private final MongoDatasetRepository datasetRepository;

  private final MongoRecordRepository recordRepository;

  private final MongoClusterRepository clusterRepository;

  private final MongoGroundTruthRepository groundTruthRepository;

  private final DatasetIdService datasetIdService;

  public DatasetMongoService(
          MongoTemplate mongoTemplate,
          MongoDatasetRepository datasetRepository,
          MongoRecordRepository recordRepository,
          MongoClusterRepository clusterRepository,
          MongoGroundTruthRepository groundTruthRepository, DatasetIdService datasetIdService) {
    this.mongoTemplate = mongoTemplate;
    this.datasetRepository = datasetRepository;
    this.recordRepository = recordRepository;
    this.clusterRepository = clusterRepository;
    this.groundTruthRepository = groundTruthRepository;
    this.datasetIdService = datasetIdService;
  }

  public DatabaseBlockedDataSet getBlockedDataSet(long datasetId) {
    DatabaseBlockedDataSet dataset = new DatabaseBlockedDataSet(this);
    dataset.setDatasetId(datasetId);
    return dataset;
  }

  public void addRecord(long datasetId, MongoRecord record) {
    checkIfDatasetDoesNotExist(datasetId, true);
    record.setDatasetId(datasetId);
    recordRepository.save(record);
  }

  public void addRecords(long datasetId, Collection<MongoRecord> records) {
    checkIfDatasetDoesNotExist(datasetId, true);
    records.forEach(r -> r.setDatasetId(datasetId));
    recordRepository.saveAll(records);
  }

  public void addGroundTruth(MongoGroundTruth groundTruth) {
    groundTruthRepository.save(groundTruth);
  }

  public Optional<MongoGroundTruth> getGroundTruth(long datasetId) {
    return groundTruthRepository.findByDatasetId(datasetId);
  }

  public Optional<MongoRecord> getRecord(long datasetId, RecordId id) {
    return recordRepository.findByDatasetIdAndSourceAndLocal(datasetId, id.getSourceId(), id.getLocalId()
    );
  }

  public Optional<MongoRecord> getRecord(String uniqueId) {
    return recordRepository.findById(new ObjectId(uniqueId));
  }

  public List<MongoRecord> getRecords(Collection<String> uniqueIds) {
    List<ObjectId> dbIds = uniqueIds.stream().map(ObjectId::new).collect(Collectors.toList());
    return recordRepository.findAllById(dbIds);
  }

  public List<MongoDataset> getDatasets(Optional<Long> plaintextDatasetId) {
    if (plaintextDatasetId.isPresent()) {
      return datasetRepository.findByPlaintextDatasetId(plaintextDatasetId.get());
    }
    return datasetRepository.findAll();
  }

  public void deleteDataset(long datasetId) {
    Optional<MongoDataset> byDatasetId = datasetRepository.findByDatasetId(datasetId);
    byDatasetId.ifPresent(datasetRepository::delete);
  }

  public boolean checkIfDatasetDoesNotExist(long datasetId, boolean throwException) {
    if (datasetRepository.findByDatasetId(datasetId).isEmpty()) {
      if (throwException) {
        throw new RuntimeException(String.format("Dataset with id %s does not exist. Create it first.", datasetId));
      }
      log.warn("Dataset with id {} does not exist.", datasetId);
      return false;
    }
    return true;
  }

  public MongoDataset addDataset(MongoDataset dataset) {
    if (dataset.getDatasetId() == null || dataset.getDatasetId() == 0) {
      Set<Long> existingIds = getDatasets(Optional.empty()).stream()
              .map(MongoDataset::getDatasetId)
              .collect(Collectors.toCollection(TreeSet::new));

      long newId = 10000;    // Offset for auto generated dataset id
      while (existingIds.contains(newId)) {
        newId++;
      }
      dataset.setDatasetId(newId);
    }
    return datasetRepository.save(dataset);
  }

  public Optional<MongoDataset> getDataset(long datasetId) {
    return datasetRepository.findByDatasetId(datasetId);
  }

  public List<Long> getDatasetIds() {
    return mongoTemplate.getCollection(mongoTemplate.getCollectionName(MongoRecord.class))
            .distinct("datasetId", Long.class).into(new ArrayList<>());
  }

  public Collection<Record> getAllRecords(long datasetId) {
    return recordRepository.findByDatasetIdOrderByObjectId(datasetId);
  }

  public Collection<Record> getRecordsBySource(long datasetId, String sourceName) {
    return recordRepository.findByDatasetIdAndSourceOrderByObjectId(datasetId, sourceName);
  }

  public void addClusters(ObjectId projectId, Collection<MongoCluster> clusters) {
    clusters.forEach(c -> c.setProjectId(projectId));
    clusterRepository.saveAll(clusters);
  }

  public Collection<MongoCluster> getClusters(ObjectId projectId) {
    return clusterRepository.findByProjectId(projectId);
  }

  public void deleteClusters(ObjectId projectId) {
    clusterRepository.deleteByProjectId(projectId);
  }

  public void addBlockedRecord(ObjectId projectId, Record record, Collection<BlockingKey> blockingKeys) {
    List<MongoRecord> records = new ArrayList<>();
    records.add((MongoRecord) record);
    MongoCluster mongoCluster = new MongoCluster(records, new HashSet<>(blockingKeys));
    mongoCluster.setProjectId(projectId);
    clusterRepository.save(mongoCluster);
  }

  public Collection<Record> getRecordsByBlockingKey(ObjectId projectId, BlockingKey bk) {
    return getRepresentatives(getClustersByBlockingKey(projectId, bk));
  }

  public Collection<MongoCluster> getClustersByBlockingKey(ObjectId projectId, BlockingKey bk) {
    Collection<MongoCluster> foundCluster =
            clusterRepository.findByProjectIdAndBlockingKeysContains(projectId, bk);
    return foundCluster;
  }

  public Collection<Record> getRecordsByBlockingKeys(ObjectId projectId, Collection<BlockingKey> bks) {
    return getRepresentatives(getClustersByBlockingKeys(projectId, bks));
  }

  public Collection<MongoCluster> getClustersByBlockingKeys(ObjectId projectId, Collection<BlockingKey> bks) {
    if (bks.isEmpty()) {
      Collection<MongoCluster> allRecords = clusterRepository.findByProjectId(projectId);
      return allRecords;
    }

    Collection<MongoCluster> foundCluster =
            clusterRepository.findByProjectIdAndBlockingKeysContaining(projectId, new HashSet<>(bks));
    return foundCluster;
  }

  private static List<Record> getRepresentatives(Collection<MongoCluster> foundCluster) {
    return foundCluster.stream().map(MongoCluster::getRepresentative).collect(Collectors.toList());
  }

  public long size(long datasetId) {
    return recordRepository.countByDatasetId(datasetId);
  }

  public void deleteAll(long datasetId) {
    recordRepository.deleteAllByDatasetId(datasetId);
  }
}

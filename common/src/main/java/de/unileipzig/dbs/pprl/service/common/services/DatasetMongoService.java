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
@Service
public class DatasetMongoService {

  private final MongoTemplate mongoTemplate;

  private final MongoDatasetRepository datasetRepository;

  private final MongoRecordRepository recordRepository;

  private final MongoClusterRepository clusterRepository;

  private final MongoGroundTruthRepository groundTruthRepository;

  public DatasetMongoService(
    MongoTemplate mongoTemplate,
    MongoDatasetRepository datasetRepository,
    MongoRecordRepository recordRepository,
    MongoClusterRepository clusterRepository,
    MongoGroundTruthRepository groundTruthRepository) {
    this.mongoTemplate = mongoTemplate;
    this.datasetRepository = datasetRepository;
    this.recordRepository = recordRepository;
    this.clusterRepository = clusterRepository;
    this.groundTruthRepository = groundTruthRepository;
  }

  public DatabaseBlockedDataSet getBlockedDataSet(int idDataset) {
    DatabaseBlockedDataSet dataset = new DatabaseBlockedDataSet(this);
    dataset.setIdDataset(idDataset);
    return dataset;
  }

  public void addRecord(int idDataset, MongoRecord record) {
    record.setIdDataset(idDataset);
    recordRepository.save(record);
  }

  public void addRecords(int idDataset, Collection<MongoRecord> records) {
    records.forEach(r -> r.setIdDataset(idDataset));
    recordRepository.saveAll(records);
  }

  public void addGroundTruth(MongoGroundTruth groundTruth) {
    groundTruthRepository.save(groundTruth);
  }

  public Optional<MongoGroundTruth> getGroundTruth(int idDataset) {
    return groundTruthRepository.findByDatasetId(idDataset);
  }

  public Optional<MongoRecord> getRecord(int idDataset, RecordId id) {
    return recordRepository.findByIdDatasetAndSourceAndLocal(idDataset, id.getSourceId(), id.getLocalId()
    );
  }

  public Optional<MongoRecord> getRecord(String uniqueId) {
    return recordRepository.findById(new ObjectId(uniqueId));
  }

  public List<MongoRecord> getRecords(Collection<String> uniqueIds) {
    List<ObjectId> dbIds = uniqueIds.stream().map(ObjectId::new).collect(Collectors.toList());
    return (List<MongoRecord>) recordRepository.findAllById(dbIds);
  }

  public List<MongoDataset> getDatasets(Optional<Integer> plaintextDatasetId) {
    if (plaintextDatasetId.isPresent()) {
      return datasetRepository.findByPlaintextDatasetId(plaintextDatasetId.get());
    }
    return datasetRepository.findAll();
  }

  public  void deleteDataset(int idDataset) {
    Optional<MongoDataset> byDatasetId = datasetRepository.findByDatasetId(idDataset);
    byDatasetId.ifPresent(datasetRepository::delete);
  }

  public MongoDataset addDataset(MongoDataset dataset) {
    if (dataset.getDatasetId() == 0) {
      Set<Integer> existingIds = getDatasets(Optional.empty()).stream()
        .map(MongoDataset::getDatasetId)
        .collect(Collectors.toCollection(TreeSet::new));

      int newId = 10000;
      while (existingIds.contains(newId)) {
        newId++;
      }
      dataset.setDatasetId(newId);
    }
    return datasetRepository.save(dataset);
  }

  public Optional<MongoDataset> getDataset(int idDataset) {
    return datasetRepository.findByDatasetId(idDataset);
  }

  public List<Integer> getDatasetIds() {
    return mongoTemplate.getCollection(mongoTemplate.getCollectionName(MongoRecord.class))
      .distinct("idDataset", Integer.class).into(new ArrayList<>());
  }

  public Collection<Record> getAllRecords(int idDataset) {
    return recordRepository.findByIdDataset(idDataset);
  }

  public Collection<Record> getRecordsBySource(int idDataset, String sourceName) {
    return recordRepository.findByIdDatasetAndSource(idDataset, sourceName);
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

  public long size(int idDataset) {
    return recordRepository.countByIdDataset(idDataset);
  }

  public void deleteAll(int idDataset) {
    recordRepository.deleteAllByIdDataset(idDataset);
  }
}

package de.unileipzig.dbs.pprl.service.common.dataset;

import de.unileipzig.dbs.pprl.core.common.model.api.BlockedDataSet;
import de.unileipzig.dbs.pprl.core.common.model.api.BlockingKey;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordId;
import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoRecord;
import de.unileipzig.dbs.pprl.service.common.services.DatasetMongoService;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A dataset backed by a database.
 */
public class DatabaseBlockedDataSet implements BlockedDataSet {

  private int idDataset;

  protected ObjectId projectId;

  private final DatasetMongoService datasetMongoService;

  public DatabaseBlockedDataSet(DatasetMongoService datasetMongoService) {
    this.datasetMongoService = datasetMongoService;
  }

  public int getIdDataset() {
    return idDataset;
  }

  public void setIdDataset(int idDataset) {
    this.idDataset = idDataset;
  }

  public ObjectId getProjectId() {
    return projectId;
  }

  public void setProjectId(ObjectId projectId) {
    this.projectId = projectId;
  }

  @Override
  public void addRecord(Record record) {
    datasetMongoService.addRecord(idDataset, (MongoRecord) record);
  }

  @Override
  public void addRecords(Collection<Record> records) {
    datasetMongoService.addRecords(idDataset,
      records.stream().map(r -> (MongoRecord) r).collect(Collectors.toList()));
  }

  @Override
  public Optional<Record> getRecord(RecordId id) {
    return cast(datasetMongoService.getRecord(idDataset, id));
  }

  public Optional<Record> getRecord(String uniqueId) {
    return cast(datasetMongoService.getRecord(uniqueId));
  }

  public List<Record> getRecords(Collection<String> uniqueIds) {
    return new ArrayList<>(datasetMongoService.getRecords(uniqueIds));
  }

  private Optional<Record> cast(Optional<MongoRecord> record) {
    return record.map(r -> r);
  }

  @Override
  public Collection<Record> getAllRecords() {
    return datasetMongoService.getAllRecords(idDataset);
  }

  @Override
  public Collection<Record> getRecordsBySource(String sourceName) {
    return datasetMongoService.getRecordsBySource(idDataset, sourceName);
  }

  @Override
  public void addBlockedRecord(Record record, Collection<BlockingKey> blockingKeys) {
    datasetMongoService.addBlockedRecord(projectId, record, blockingKeys);
  }

  @Override
  public Collection<Record> getRecordsByBlockingKey(BlockingKey bk) {
    return datasetMongoService.getRecordsByBlockingKey(projectId, bk);
  }

  @Override
  public Collection<Record> getRecordsByBlockingKeys(Collection<BlockingKey> bks) {
    return datasetMongoService.getRecordsByBlockingKeys(projectId, bks);
  }

  @Override
  public long size() {
    return datasetMongoService.size(idDataset);
  }

  public void deleteAll() {
    datasetMongoService.deleteAll(idDataset);
  }
}

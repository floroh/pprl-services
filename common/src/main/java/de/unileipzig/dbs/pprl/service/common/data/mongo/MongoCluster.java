package de.unileipzig.dbs.pprl.service.common.data.mongo;

import de.unileipzig.dbs.pprl.core.common.model.api.BlockingKey;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordCluster;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@Document
public class MongoCluster implements RecordCluster {

  @Id
  private ObjectId objectId;

  @Indexed
  private ObjectId projectId;

  @Singular
  @DocumentReference
  private Collection<MongoRecord> records;

  private MongoRecord representative;

  @Indexed
  private Set<BlockingKey> blockingKeys;

  public MongoCluster(Collection<MongoRecord> records) {
    this.records = records;
  }

  public MongoCluster(Collection<MongoRecord> records, Set<BlockingKey> blockingKeys) {
    this.records = records;
    this.blockingKeys = blockingKeys;
  }

  private MongoCluster(ObjectId objectId, ObjectId projectId, Collection<MongoRecord> records,
    MongoRecord representative, Set<BlockingKey> blockingKeys) {
    this.objectId = objectId;
    this.projectId = projectId;
    this.records = records;
    this.representative = representative;
    this.blockingKeys = blockingKeys;
  }

  public MongoCluster() {
    records = new ArrayList<>();
  }

  @Override
  public void addRecord(Record record) {
    records.add((MongoRecord) record);
  }

  @Override
  public Collection<Record> getRecords() {
    return new ArrayList<>(records);
  }

  public MongoRecord getRepresentative() {
    if (representative == null) {
      representative = records.iterator().next();
    }
    return representative;
  }

  public void addBlockingKey(BlockingKey blockingKey) {
    if (this.blockingKeys == null) {
      this.blockingKeys = new HashSet<>();
    }
    this.blockingKeys.add(blockingKey);
  }
}

package de.unileipzig.dbs.pprl.service.common.persistence.repositories.mongo;

import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoRecord;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Collection;
import java.util.Optional;

public interface MongoRecordRepository extends MongoRepository<MongoRecord, ObjectId> {

  @Query("{'datasetId': ?0, 'recordId.ids.SOURCE_ID': ?1, 'recordId.ids.LOCAL_ID': ?2}")
  Optional<MongoRecord> findByDatasetIdAndSourceAndLocal(long datasetId, String source, String local);

  @Query("{'datasetId': ?0, 'recordId.ids.SOURCE_ID': ?1}")
  Collection<Record> findByDatasetIdAndSourceOrderByObjectId(long datasetId, String source);

  Collection<Record> findByDatasetIdOrderByObjectId(long datasetId);

  long countByDatasetId(long datasetId);

  void deleteAllByDatasetId(long datasetId);

}

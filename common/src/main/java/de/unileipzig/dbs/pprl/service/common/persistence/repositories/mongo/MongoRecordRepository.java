package de.unileipzig.dbs.pprl.service.common.persistence.repositories.mongo;

import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoRecord;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Collection;
import java.util.Optional;

public interface MongoRecordRepository extends MongoRepository<MongoRecord, ObjectId> {

  @Query("{'idDataset': ?0, 'recordId.ids.SOURCE_ID': ?1, 'recordId.ids.LOCAL_ID': ?2}")
  Optional<MongoRecord> findByIdDatasetAndSourceAndLocal(int idDataset, String source, String local);

  @Query("{'idDataset': ?0, 'recordId.ids.SOURCE_ID': ?1}")
  Collection<Record> findByIdDatasetAndSource(int idDataset, String source);

  Collection<Record> findByIdDataset(int idDataset);

  long countByIdDataset(int idDataset);

  void deleteAllByIdDataset(int idDataset);

}

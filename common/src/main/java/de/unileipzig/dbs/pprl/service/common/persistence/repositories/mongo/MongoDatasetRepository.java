package de.unileipzig.dbs.pprl.service.common.persistence.repositories.mongo;

import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoDataset;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface MongoDatasetRepository extends MongoRepository<MongoDataset, ObjectId> {

  Optional<MongoDataset> findByDatasetId(int idDataset);

  List<MongoDataset> findByPlaintextDatasetId(int plaintextDatasetId);
}

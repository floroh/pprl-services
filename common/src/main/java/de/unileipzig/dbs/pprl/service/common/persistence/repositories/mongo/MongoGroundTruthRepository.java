package de.unileipzig.dbs.pprl.service.common.persistence.repositories.mongo;

import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoGroundTruth;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MongoGroundTruthRepository extends MongoRepository<MongoGroundTruth, ObjectId> {

  Optional<MongoGroundTruth> findByDatasetId(int idDataset);

}

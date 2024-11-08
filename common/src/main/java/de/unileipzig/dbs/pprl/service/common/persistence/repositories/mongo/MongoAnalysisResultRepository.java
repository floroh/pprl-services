package de.unileipzig.dbs.pprl.service.common.persistence.repositories.mongo;

import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoAnalysisResult;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.Optional;

public interface MongoAnalysisResultRepository extends MongoRepository<MongoAnalysisResult, ObjectId> {

  Collection<MongoAnalysisResult> findByDatasetId(int datasetId);
  Optional<MongoAnalysisResult> findByDatasetIdAndSourceAndType(int datasetId,
    String source, MongoAnalysisResult.Type type);

}

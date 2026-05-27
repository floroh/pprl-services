package de.unileipzig.dbs.pprl.service.common.persistence.repositories.mongo;

import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoTag;
import lombok.NonNull;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;

public interface MongoTagRepository extends MongoRepository<MongoTag, ObjectId> {

  Collection<MongoTag> findByDatasetId(@NonNull long datasetId);

  Collection<MongoTag> findByDatasetIdAndOrigin(@NonNull long datasetId, String origin);

  void deleteByDatasetId(@NonNull long datasetId);
}

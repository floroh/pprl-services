package de.unileipzig.dbs.pprl.service.common.persistence.repositories.mongo;

import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoFrequencyLookup;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;


public interface MongoFrequencyLookupRepository extends MongoRepository<MongoFrequencyLookup, ObjectId> {

  Optional<MongoFrequencyLookup> findByDatasetId(int datasetId);

}

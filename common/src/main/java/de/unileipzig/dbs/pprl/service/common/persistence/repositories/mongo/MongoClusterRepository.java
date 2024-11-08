package de.unileipzig.dbs.pprl.service.common.persistence.repositories.mongo;

import de.unileipzig.dbs.pprl.core.common.model.api.BlockingKey;
import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoCluster;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.Set;

public interface MongoClusterRepository extends MongoRepository<MongoCluster, ObjectId> {

  Collection<MongoCluster> findByProjectId(ObjectId projectId);

  Collection<MongoCluster> findByProjectIdAndBlockingKeysContains(ObjectId projectId,
    BlockingKey blockingKeys);

  Collection<MongoCluster> findByProjectIdAndBlockingKeysContaining(ObjectId projectId,
    Set<BlockingKey> blockingKeys);

  void deleteByProjectId(ObjectId projectId);

}

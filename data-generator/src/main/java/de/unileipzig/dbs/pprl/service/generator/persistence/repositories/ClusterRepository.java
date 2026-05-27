package de.unileipzig.dbs.pprl.service.generator.persistence.repositories;

import de.unileipzig.dbs.pprl.service.generator.selection.model.common.RecordCluster;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ClusterRepository<T extends RecordCluster> extends MongoRepository<T, ObjectId> {

  /**
   * Returns a list of all document _id values (loads into memory).
   * Uses a projection so the driver reads only the _id field.
   */
  @Query(value = "{}", fields = "{ '_id' : 1 }")
  List<ObjectId> findAllIds();

}


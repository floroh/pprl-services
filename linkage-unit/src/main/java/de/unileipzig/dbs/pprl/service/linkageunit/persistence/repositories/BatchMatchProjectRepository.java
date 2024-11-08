package de.unileipzig.dbs.pprl.service.linkageunit.persistence.repositories;

import de.unileipzig.dbs.pprl.service.linkageunit.data.mongo.BatchMatchProject;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface BatchMatchProjectRepository extends MongoRepository<BatchMatchProject, ObjectId> {
  
}

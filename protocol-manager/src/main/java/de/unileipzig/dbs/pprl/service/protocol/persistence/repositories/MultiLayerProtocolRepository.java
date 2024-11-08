package de.unileipzig.dbs.pprl.service.protocol.persistence.repositories;

import de.unileipzig.dbs.pprl.service.protocol.model.mongo.MultiLayerProtocol;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface MultiLayerProtocolRepository extends MongoRepository<MultiLayerProtocol, ObjectId> {
  
}

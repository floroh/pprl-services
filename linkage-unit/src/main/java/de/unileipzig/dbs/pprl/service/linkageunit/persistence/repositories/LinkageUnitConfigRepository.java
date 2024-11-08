package de.unileipzig.dbs.pprl.service.linkageunit.persistence.repositories;

import de.unileipzig.dbs.pprl.service.linkageunit.data.mongo.LinkageConfig;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;

public interface LinkageUnitConfigRepository extends MongoRepository<LinkageConfig, ObjectId> {

  Collection<LinkageConfig> findAllByPartial(boolean partial);

  Collection<LinkageConfig> findByPartialAndMatchingDto_Id_Method(boolean partial, String method);

  void deleteAllByMatchingDto_Id_Method(String method);

}

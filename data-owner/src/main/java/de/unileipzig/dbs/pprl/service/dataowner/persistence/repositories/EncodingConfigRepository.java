package de.unileipzig.dbs.pprl.service.dataowner.persistence.repositories;

import de.unileipzig.dbs.pprl.service.dataowner.data.mongo.MongoEncodingConfig;
import lombok.NonNull;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;

public interface EncodingConfigRepository extends MongoRepository<MongoEncodingConfig, ObjectId> {

  Collection<MongoEncodingConfig> findMongoEncodingConfigsByEncodingDto_Id_Method(@NonNull String method);

  void deleteAllByEncodingDto_Id_Method(@NonNull String method);

  void deleteAllByEncodingDto_Id_MethodAndEncodingDto_Id_Project(@NonNull String method, @NonNull String project);
}

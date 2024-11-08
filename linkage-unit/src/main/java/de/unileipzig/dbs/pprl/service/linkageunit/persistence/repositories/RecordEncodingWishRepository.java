package de.unileipzig.dbs.pprl.service.linkageunit.persistence.repositories;

import de.unileipzig.dbs.pprl.service.common.data.dto.RecordEncodingWishDto;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RecordEncodingWishRepository extends MongoRepository<RecordEncodingWishDto, ObjectId> {

  List<RecordEncodingWishDto> findAllByEncodingId_ProjectOrderByOrderId(String projectId);

  void deleteAllByEncodingId_Project(String projectId);
}

package de.unileipzig.dbs.pprl.service.linkageunit.persistence.repositories;

import de.unileipzig.dbs.pprl.core.common.model.impl.MatchGrade;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoRecordPair;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RecordPairRepository extends MongoRepository<MongoRecordPair, ObjectId> {

  List<MongoRecordPair> findMongoRecordPairByClassification(MatchGrade classification);

  @Query(value = "{ 'projectId' : ?0 }", fields = "{" +
    "'leftRecord.idDataset' : 0, 'leftRecord.encodingId' : 0, 'leftRecord.stringAttributes' : 0," +
    "'rightRecord.idDataset' : 0, 'rightRecord.encodingId' : 0, 'rightRecord.stringAttributes' : 0 }")
  List<RecordPair> findMongoRecordPairByProjectIdNoRecords(ObjectId projectId);

//  Iterator<RecordPair> findMongoRecordPairByProjectIdAsIterator(ObjectId projectId);

  @Query("{ 'projectId' : ?0 , 'properties': { $all: ?1 } }")
  List<RecordPair> findMongoRecordPairByProjectIdAndPropertiesAll(ObjectId projectId, Set<String> properties);

  List<RecordPair> findMongoRecordPairByProjectIdAndPropertiesContains(ObjectId projectId,
    Set<String> properties);

  List<RecordPair> findMongoRecordPairByProjectId(ObjectId projectId);

  List<RecordPair> findMongoRecordPairByProjectIdAndClassificationIsIn(
    ObjectId projectId, Collection<MatchGrade> classification);

  Optional<MongoRecordPair> findMongoRecordPairByProjectIdAndPairId(ObjectId projectId, String pairId);

  List<MongoRecordPair> findMongoRecordPairByProjectIdAndPairIdIn(ObjectId projectId,
    Collection<String> pairId);

  long countByProjectId(ObjectId projectId);

  void deleteByProjectId(ObjectId projectId);

  void deleteByProjectIdAndSimilarityLessThan(ObjectId projectId, double similarity);

}

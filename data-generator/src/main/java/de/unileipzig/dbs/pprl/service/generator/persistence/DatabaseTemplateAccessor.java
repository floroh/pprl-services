package de.unileipzig.dbs.pprl.service.generator.persistence;

import de.unileipzig.dbs.pprl.service.generator.selection.model.common.ClusterOrder;
import de.unileipzig.dbs.pprl.service.generator.selection.model.common.RecordCluster;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Service
public class DatabaseTemplateAccessor {

  public DatabaseTemplateAccessor(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  private final MongoTemplate mongoTemplate;

  public List<ObjectId> findAllClusterIds(Class<? extends RecordCluster> clazz) {
    Query query = new Query();
    query.fields().include("_id");

    return mongoTemplate.find(query, clazz)
            .stream()
            .map(RecordCluster::getId)
            .toList();
  }

  public Stream<ClusterOrder> getClusterOrderStream(int batchSize, String seed, String clusterType) {
    Query q = Query.query(Criteria.where("seed").is(seed)
                    .and("clusterType").is(clusterType))
            .with(Sort.by(Sort.Direction.ASC, "sortKey"))
            .cursorBatchSize(batchSize);

    // mongoTemplate.stream returns Stream<ClusterOrder>
    return mongoTemplate.stream(q, ClusterOrder.class, "cluster_orders");
  }

  public Stream<? extends RecordCluster> getClusterSteamInDatabaseOrder(int batchSize, Class<? extends RecordCluster> clazz) {
    Query q = new Query().cursorBatchSize(batchSize);
    return mongoTemplate.stream(q, clazz);
  }
}

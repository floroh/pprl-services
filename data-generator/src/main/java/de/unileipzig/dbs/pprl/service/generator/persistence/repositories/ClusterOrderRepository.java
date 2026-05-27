package de.unileipzig.dbs.pprl.service.generator.persistence.repositories;

import de.unileipzig.dbs.pprl.service.generator.selection.model.common.ClusterOrder;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ClusterOrderRepository extends MongoRepository<ClusterOrder, String> {

  boolean existsBySeedAndClusterType(String seed, String clusterType);

  List<ClusterOrder> findBySeedAndClusterTypeOrderBySortKeyAsc(String seed, String clusterType);

  void deleteClusterOrderByClusterTypeAndSeed(String clusterType, String seed);
}

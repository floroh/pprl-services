package de.unileipzig.dbs.pprl.service.generator.persistence.repositories;

import de.unileipzig.dbs.pprl.service.generator.selection.model.common.ClusterType;
import de.unileipzig.dbs.pprl.service.generator.selection.model.ncvr.NcvrRecordCluster;


public interface NcvrClusterRepository extends ClusterRepository<NcvrRecordCluster>, ClusterMetadata {
  @Override
  default ClusterType getClusterType() {
    return ClusterType.NC;
  }
}

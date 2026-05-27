package de.unileipzig.dbs.pprl.service.generator.persistence;

import de.unileipzig.dbs.pprl.service.generator.persistence.repositories.ClusterMetadata;
import de.unileipzig.dbs.pprl.service.generator.persistence.repositories.ClusterRepository;
import de.unileipzig.dbs.pprl.service.generator.selection.model.common.ClusterType;
import de.unileipzig.dbs.pprl.service.generator.selection.model.common.RecordCluster;
import de.unileipzig.dbs.pprl.service.generator.selection.model.ncvr.NcvrRecordCluster;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RepositorySelector {

  private final Map<ClusterType, ClusterRepository<? extends RecordCluster>> repositories;

  public RepositorySelector(List<ClusterRepository<? extends RecordCluster>> clusterRepositories) {
    this.repositories = clusterRepositories.stream()
            .filter(repo -> repo instanceof ClusterMetadata) // only those with metadata
            .map(repo -> (ClusterRepository<? extends RecordCluster> & ClusterMetadata) repo)
            .collect(Collectors.toMap(
                    ClusterMetadata::getClusterType,
                    repo -> repo
            ));
  }

  public ClusterRepository<? extends RecordCluster> getRepository(ClusterType type) {
    ClusterRepository<? extends RecordCluster> repository = repositories.get(type);
    if (repository == null) throw new IllegalArgumentException("No repo for " + type);
    return repository;
  }


  public Class<? extends RecordCluster> getEntityClass(ClusterType type) {
    return switch (type) {
      case NC -> NcvrRecordCluster.class;
    };
  }
}

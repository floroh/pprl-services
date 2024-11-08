package de.unileipzig.dbs.pprl.service.linkageunit.dataset;

import de.unileipzig.dbs.pprl.core.common.HashUtils;
import de.unileipzig.dbs.pprl.core.common.model.api.BlockingKey;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordCluster;
import de.unileipzig.dbs.pprl.core.matcher.blocking.BlockingGroup;
import de.unileipzig.dbs.pprl.core.matcher.model.api.LinkageProcessDataSet;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoCluster;
import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoRecord;
import de.unileipzig.dbs.pprl.service.common.dataset.DatabaseBlockedDataSet;
import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoRecordPair;
import de.unileipzig.dbs.pprl.service.linkageunit.config.LinkageUnitReportingConfig;
import de.unileipzig.dbs.pprl.service.linkageunit.services.ProjectService;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class DatabaseLinkageProcessDataset extends DatabaseBlockedDataSet implements LinkageProcessDataSet {

  private ProjectService projectService;

  private final LinkageUnitReportingConfig linkageUnitReportingConfig;

  public DatabaseLinkageProcessDataset(ProjectService projectService, LinkageUnitReportingConfig linkageUnitReportingConfig) {
    super(projectService.getDatasetService());
    this.projectService = projectService;
    this.linkageUnitReportingConfig = linkageUnitReportingConfig;
  }

  @Override
  public void addBlockingGroups(Collection<BlockingGroup> blockingGroups) {
    if (linkageUnitReportingConfig.isSkipBlockingReports()) {
      log.debug("Skip building blocking clusters, because blocking reports are skipped");
      return;
    }
    log.info("Building clusters from {} blocking groups", blockingGroups.size());
    Map<Long, MongoCluster> clusterByGroupId = new HashMap<>();
    for (BlockingGroup blockingGroup : blockingGroups) {
      long groupId = blockingGroup.getGroups().stream()
        .flatMap(r -> r.getRecords().stream())
        .map(record -> record.getId().getUniqueId())
        .mapToLong(HashUtils::getSHALongHash)
        .sum();
      if (clusterByGroupId.containsKey(groupId)) {
        clusterByGroupId.get(groupId).addBlockingKey(blockingGroup.getBlockingKey());
      } else {
        HashSet<BlockingKey> blockingKeys = new HashSet<>();
        blockingKeys.add(blockingGroup.getBlockingKey());
        MongoCluster newCluster = MongoCluster.builder()
          .projectId(projectId)
          .records(blockingGroup.getGroups().stream()
            .flatMap(g -> g.getRecords().stream().map(record -> (MongoRecord) record))
            .collect(Collectors.toList()))
          .blockingKeys(blockingKeys)
          .build();
        clusterByGroupId.put(groupId, newCluster);
      }
    }
    log.info("Adding {} clusters", clusterByGroupId.size());
    projectService.getDatasetService().addClusters(projectId, clusterByGroupId.values());
  }


  @Override
  public void addRecordPair(RecordPair recordPair) {
    MongoRecordPair mongoRecordPair = toMongoRecordPair(recordPair);
    projectService.addRecordPair(projectId, mongoRecordPair);
  }

  @Override
  public void addRecordPairs(Collection<RecordPair> recordPairs) {
    projectService.addRecordPairs(projectId, recordPairs.stream()
      .map(this::toMongoRecordPair)
      .peek(DatabaseLinkageProcessDataset::updateActiveProperty)
      .collect(Collectors.toList())
    );
  }

  @Override
  public long getRecordPairCount() {
    return projectService.getRecordPairCount(projectId);
  }

  @Override
  public void cleanRecordPairs() {
    projectService.cleanRecordPairs(projectId);
  }

  @Override
  public Collection<RecordPair> getRecordPairs() {
    return projectService.getRecordPairs(projectId);
  }

  @Override
  public Collection<RecordPair> getClassifiedRecordPairs() {
    return projectService.getClassifiedRecordPairs(projectId);
  }

  @Override
  public Collection<RecordPair> getRecordPairsFilteredByProperties(Set<String> properties) {
    return projectService.getRecordPairsFilteredByProperties(projectId, properties);
  }

  @Override
  public void updateRecordPairs(Collection<RecordPair> recordPairs) {
    List<MongoRecordPair> rps = recordPairs.stream()
      .map(this::toMongoRecordPair)
      .peek(DatabaseLinkageProcessDataset::updateActiveProperty)
      .collect(Collectors.toList());
    projectService.addRecordPairs(projectId, rps);
  }

  @Override
  public void replaceRecordPairs(Collection<RecordPair> recordPairs) {
    List<MongoRecordPair> rps = recordPairs.stream()
      .map(this::toMongoRecordPair)
      .peek(DatabaseLinkageProcessDataset::updateActiveProperty)
      .collect(Collectors.toList());
    projectService.replaceRecordPairs(projectId, rps);
  }

  public static void updateActiveProperty(MongoRecordPair rp) {
    if (rp.getTags().stream().anyMatch(t ->
      List.of(TAG_REMOVED_BY_CLASSIFIER, TAG_REMOVED_BY_POSTPROCESSING).contains(t.getTag()))) {
      rp.removeProperty(ACTIVE);
    } else {
      rp.addProperty(ACTIVE);
    }
  }

  @Override
  public void addRecordCluster(RecordCluster recordCluster) {
    MongoCluster mongoRecordCluster = toMongoRecordCluster(recordCluster);
    projectService.addRecordCluster(projectId, mongoRecordCluster);
  }

  private MongoCluster toMongoRecordCluster(RecordCluster recordCluster) {
    if (recordCluster instanceof MongoCluster) {
      return (MongoCluster) recordCluster;
    }
    log.warn("Converting RecordCluster to MongoRecordCluster");
    return MongoCluster.builder()
      .projectId(projectId)
      .records(recordCluster.getRecords().stream().map(r -> (MongoRecord) r).collect(Collectors.toList()))
      .build();
  }

  private MongoRecordPair toMongoRecordPair(RecordPair recordPair) {
    if (recordPair instanceof MongoRecordPair) {
      return (MongoRecordPair) recordPair;
    }
    log.warn("Converting RecordPair to MongoRecordPair");
    MongoRecordPair mongoRecordPair =
      new MongoRecordPair(
        (MongoRecord) recordPair.getLeftRecord(), (MongoRecord) recordPair.getRightRecord());
    mongoRecordPair.setClassification(recordPair.getClassification());
    mongoRecordPair.setSimilarity(recordPair.getSimilarity());
    recordPair.getTags().forEach(mongoRecordPair::addTag);
    return mongoRecordPair;
  }
}

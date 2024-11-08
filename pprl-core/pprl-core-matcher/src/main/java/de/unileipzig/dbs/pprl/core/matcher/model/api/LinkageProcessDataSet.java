package de.unileipzig.dbs.pprl.core.matcher.model.api;

import de.unileipzig.dbs.pprl.core.common.model.api.BlockedDataSet;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordCluster;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import de.unileipzig.dbs.pprl.core.matcher.blocking.BlockingGroup;

import java.util.Collection;
import java.util.Set;

/**
 * Dataset that not only contains records, but also blocking keys and record links
 */
public interface LinkageProcessDataSet extends BlockedDataSet {

  String TAG_REMOVED_BY_CLASSIFIER = "removedByClassifier";
  String TAG_REMOVED_BY_POSTPROCESSING = "removedByPostprocessing";

  //TODO Place property names in an enum
  String ACTIVE = "active";
  String NEW = "new";
  String REPLACED = "replaced";

  void addBlockingGroups(Collection<BlockingGroup> blockingGroups);

  void addRecordPair(RecordPair recordPair);

  default void addRecordPairs(Collection<RecordPair> recordPairs) {
    recordPairs.forEach(this::addRecordPair);
  }

  long getRecordPairCount();

  default void cleanRecordPairs() {
  }

  Collection<RecordPair> getRecordPairs();

  Collection<RecordPair> getClassifiedRecordPairs();

  Collection<RecordPair> getRecordPairsFilteredByProperties(Set<String> properties);

  default Collection<RecordPair> getRecordPairsFilteredByProperty(String property) {
    return getRecordPairsFilteredByProperties(Set.of(property));
  }

  default Collection<RecordPair> getActiveRecordPairs() {
    return getRecordPairsFilteredByProperty(ACTIVE);
  }

  void addRecordCluster(RecordCluster recordCluster);

  default void addRecordClusters(Collection<RecordCluster> recordClusters) {
    recordClusters.forEach(this::addRecordCluster);
  }

  void updateRecordPairs(Collection<RecordPair> recordPairs);

  void replaceRecordPairs(Collection<RecordPair> recordPairs);
}

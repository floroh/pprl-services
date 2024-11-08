package de.unileipzig.dbs.pprl.service.linkageunit.services.helper;

import de.unileipzig.dbs.pprl.core.common.model.api.RecordId;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordIdPair;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import de.unileipzig.dbs.pprl.core.common.model.impl.RecordPairSimple;
import de.unileipzig.dbs.pprl.core.common.monitoring.Tag;
import de.unileipzig.dbs.pprl.core.matcher.classification.Classifier;
import de.unileipzig.dbs.pprl.core.matcher.model.api.LinkageProcessDataSet;
import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoRecord;
import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoRecordPair;
import de.unileipzig.dbs.pprl.service.linkageunit.data.mongo.BatchMatchProject;
import de.unileipzig.dbs.pprl.service.linkageunit.services.LinkImprovementService;
import de.unileipzig.dbs.pprl.service.linkageunit.services.ProjectService;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.unileipzig.dbs.pprl.core.matcher.model.api.LinkageProcessDataSet.NEW;

@Slf4j
public class MongoRecordUtils {

  public static  Map<RecordId, MongoRecord> getRecordIdMongoRecordMapForNew(ProjectService projectService,
    BatchMatchProject project) {
    return projectService.getDatasetService()
      .getAllRecords(project.getDatasetId()).stream()
      .map(record -> (MongoRecord) record)
      .filter(record -> record.getProperties().contains(NEW))
      .collect(Collectors.toMap(MongoRecord::getId, record -> record));
  }

  public static Map<String, RecordPair> getRecordPairsForNew(ProjectService projectService,
    BatchMatchProject project) {
    Map<String, Set<MongoRecord>> grouped = projectService.getDatasetService()
      .getAllRecords(project.getDatasetId()).stream()
      .map(record -> (MongoRecord) record)
      .filter(record -> record.getProperties().contains(NEW))
      .collect(Collectors.groupingBy(mr -> mr.getId().getId(RecordId.BLOCK_ID), Collectors.toSet()));
    Map<String, RecordPair> pairs = grouped.values().stream()
      .flatMap(set -> {
        if (set.size() != 2) {
          log.error("Record set size is not 2");
          return Stream.empty();
        }
        return Stream.of(new RecordPairSimple(
          set.stream().findFirst().get(),
          set.stream().skip(1).findFirst().get()
        ));
      })
      .collect(Collectors.toMap(RecordPair::getPairId, pair -> pair));
    return pairs;
  }

  public static void removeNewFlagFromRecords(ProjectService projectService,
    int idDataset, Collection<RecordPair> recordPairs) {
    Collection<MongoRecord> records = recordPairs.stream()
      .flatMap(recordPair -> Stream.of((MongoRecord) recordPair.getLeftRecord(), (MongoRecord) recordPair.getRightRecord()))
      .peek(record -> record.getProperties().remove(NEW))
      .collect(Collectors.toList());
    projectService.getDatasetService().addRecords(idDataset, records);
  }

  public static <T extends RecordPair> Collection<T> removeLabelTag(Collection<T> recordPairs) {
    return recordPairs.stream()
      .peek(pair -> {
        pair.getTags().remove(Tag.create(Classifier.Label.TRUE_MATCH.name()));
        pair.getTags().remove(Tag.create(Classifier.Label.TRUE_NON_MATCH.name()));
      })
      .toList();
  }

  public static Collection<MongoRecordPair> removePairsWhereImprovedLinkIsAvailable(Collection<MongoRecordPair> recordPairs) {
    List<MongoRecordPair> mrp = recordPairs.stream()
      .map(pair -> (MongoRecordPair) pair)
      .toList();
    Map<String, List<MongoRecordPair>> pairsById = mrp.stream()
      .collect(Collectors.groupingBy(RecordIdPair::getPairId));
    recordPairs = pairsById.values().stream()
        .flatMap(pairs -> {
          Optional<MongoRecordPair> improvedPair = pairs.stream()
            .filter(pair -> pair.getProperties().contains(LinkImprovementService.PROPERTY_IMPROVED_LINK))
            .filter(pair -> !pair.getProperties().contains(LinkageProcessDataSet.REPLACED))
            .findFirst();
          if (improvedPair.isPresent()) {
            return Stream.of(improvedPair.get());
          }
          for (MongoRecordPair pair : pairs) {
            if (pair.getProperties()
              .contains(LinkImprovementService.PROPERTY_LINK_FROM_UPPER_LAYER)) {
              return Stream.of(pair);
            }
          }
          pairs = pairs.stream()
            .filter(pair -> !pair.getProperties().contains(LinkageProcessDataSet.REPLACED))
            .collect(Collectors.toList());
          if (pairs.size() > 1) {
            log.warn("More than 1 pair is not expected");
            pairs.forEach(pair -> log.warn(pair.toString()));
          }
          return pairs.stream();
          }
        ).collect(Collectors.toList());
    return recordPairs;
  }
}

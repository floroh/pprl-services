package de.unileipzig.dbs.pprl.service.linkageunit.services;

import de.unileipzig.dbs.pprl.core.common.model.impl.MatchGrade;
import de.unileipzig.dbs.pprl.core.common.monitoring.Tag;
import de.unileipzig.dbs.pprl.core.matcher.model.api.LinkageProcessDataSet;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoCluster;
import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoGroundTruth;
import de.unileipzig.dbs.pprl.core.matcher.evaluation.EvaluationUtils;
import de.unileipzig.dbs.pprl.service.common.persistence.repositories.mongo.MongoClusterRepository;
import de.unileipzig.dbs.pprl.service.common.services.DatasetMongoService;
import de.unileipzig.dbs.pprl.service.linkageunit.data.converter.BatchMatchProjectConverter;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.BatchMatchProjectDto;
import de.unileipzig.dbs.pprl.service.linkageunit.data.mongo.BatchMatchProject;
import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoRecordPair;
import de.unileipzig.dbs.pprl.service.linkageunit.data.mongo.ProjectState;
import de.unileipzig.dbs.pprl.service.linkageunit.dataset.DatabaseLinkageProcessDataset;
import de.unileipzig.dbs.pprl.service.linkageunit.persistence.repositories.BatchMatchProjectRepository;
import de.unileipzig.dbs.pprl.service.linkageunit.persistence.repositories.RecordPairRepository;
import de.unileipzig.dbs.pprl.service.linkageunit.services.reporting.ReportUpdater;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static de.unileipzig.dbs.pprl.core.matcher.model.api.LinkageProcessDataSet.*;
import static de.unileipzig.dbs.pprl.service.linkageunit.services.LinkImprovementService.PROPERTY_IMPROVED_LINK;
import static de.unileipzig.dbs.pprl.service.linkageunit.services.LinkImprovementService.PROPERTY_LINK_FROM_UPPER_LAYER;

/**
 * Service for managing match projects, such as
 * - creating/deleting projects
 * - retrieve state and results of projects
 */
@Service
@Slf4j
public class ProjectService {

  public static final String TAG_IMPROVED_LINK_COUNT = "IMPROVED_LINK_COUNT";

  public static final String CONFIG_RECORD_PAIR_LIMIT = "RECORD_PAIR_LIMIT";
  private DatasetMongoService datasetService;

  private BatchMatchProjectRepository batchMatchProjectRepository;

  private MongoClusterRepository clusterRepository;

  private RecordPairRepository recordPairRepository;

  private MongoTemplate mongoTemplate;

  private ReportUpdater reportUpdater;

  public ProjectService(
    DatasetMongoService datasetService, BatchMatchProjectRepository batchMatchProjectRepository,
    RecordPairRepository recordPairRepository, MongoClusterRepository clusterRepository,
    MongoTemplate mongoTemplate, ReportUpdater reportUpdater) {
    this.batchMatchProjectRepository = batchMatchProjectRepository;
    this.datasetService = datasetService;
    this.clusterRepository = clusterRepository;
    this.recordPairRepository = recordPairRepository;
    this.mongoTemplate = mongoTemplate;
    this.reportUpdater = reportUpdater;
  }

  public BatchMatchProject add(BatchMatchProjectDto projectDto) {
    BatchMatchProject mongoProject = BatchMatchProjectConverter.dtoToProject(projectDto);
    if (mongoProject.getDatasetId() == 0) {
      throw new RuntimeException("No dataset id set for project " + mongoProject.getProjectId());
    }
    mongoProject.setState(ProjectState.COLLECTING);
    save(mongoProject);
    return mongoProject;
  }

  public void save(BatchMatchProject project) {
    project.updateLastUpdateToCurrentTime();
    batchMatchProjectRepository.save(project);
  }

  public BatchMatchProject getProject(ObjectId projectId) {
    Optional<BatchMatchProject> optionalProject = batchMatchProjectRepository.findById(projectId);
    if (optionalProject.isEmpty()) {
      throw new RuntimeException("Project with id " + projectId + " not found");
    }
    return optionalProject.get();
  }

  public BatchMatchProjectDto getProjectDto(ObjectId projectId) {
    BatchMatchProject batchMatchProject = getProject(projectId);
    return BatchMatchProjectConverter.projectToDto(batchMatchProject);
  }

  public DatabaseLinkageProcessDataset getDataset(ObjectId projectId) {
    DatabaseLinkageProcessDataset dataset =
      new DatabaseLinkageProcessDataset(this, reportUpdater.getReportingConfig());
    dataset.setProjectId(projectId);
    dataset.setIdDataset(getProject(projectId).getDatasetId());
    return dataset;
  }

  public BatchMatchProject reset(ObjectId projectId, ProjectState targetState) {
    BatchMatchProject project = getProject(projectId);
    if (project.getState().isAtMost(targetState)) {
      log.warn("Cannot reset to state {} from state {}", targetState, project.getState());
      return project;
    }
    log.info("Resetting project {} to state {}", projectId, targetState);
    project.setState(targetState);
    switch (targetState) {
      case COLLECTING:
        deleteRecordPairs(projectId);
        project.getPhases().remove(ProjectState.BLOCKING.name());
        project.getPhases().remove(ProjectState.LINKING.name());
        project.getPhases().remove(ProjectState.CLASSIFICATION.name());
        project.getPhases().remove(ProjectState.POSTPROCESSING.name());
        project.getPhases().remove(ProjectState.CLUSTERING.name());
        break;
      case CLASSIFICATION:
        List<MongoRecordPair> updatedRecordPairs = getRecordPairs(projectId).stream()
          .map(rp -> (MongoRecordPair) rp)
          .peek(rp -> {
            if (rp.getTags().stream()
              .anyMatch(t -> t.getTag().equals(LinkageProcessDataSet.TAG_REMOVED_BY_POSTPROCESSING))) {
              rp.removeTag(LinkageProcessDataSet.TAG_REMOVED_BY_POSTPROCESSING);
              rp.addTag(LinkageProcessDataSet.ACTIVE);
            }
          })
          .collect(Collectors.toList());
        addRecordPairs(projectId, updatedRecordPairs);
        project.getPhases().remove(ProjectState.POSTPROCESSING.name());
        project.getPhases().remove(ProjectState.CLUSTERING.name());
        break;
    }
    save(project);
    return project;
  }

  public void updateProjectPhases(ObjectId projectId) {
    updateProjectPhases(getProject(projectId));
  }

  public void updateProjectPhases(BatchMatchProject batchMatchProject) {
    reportUpdater.setProjectService(this);
    reportUpdater.updateProjectPhases(batchMatchProject);
  }

  public List<BatchMatchProjectDto> getAllProjects() {
    log.debug("Retrieval of all projects");
    return batchMatchProjectRepository.findAll().stream()
      .map(BatchMatchProjectConverter::projectToDto)
      .collect(Collectors.toList());
  }

  public void deleteRecordPairs(ObjectId projectId) {
    recordPairRepository.deleteByProjectId(projectId);
  }

  public void addRecordPairs(ObjectId projectId, Collection<MongoRecordPair> recordPairs) {
    recordPairs.forEach(rp -> rp.setProjectId(projectId));
    recordPairRepository.saveAll(recordPairs);
  }

  public void addRecordPair(ObjectId projectId, MongoRecordPair recordPair) {
    recordPair.setProjectId(projectId);
    recordPairRepository.save(recordPair);
  }

  public void addRecordCluster(ObjectId projectId, MongoCluster cluster) {
    cluster.setProjectId(projectId);
    clusterRepository.save(cluster);
  }

  /**
   * Get the cumulative counts of record pairs reverse sorted by similarity.
   *
   * @param projectId project id
   * @return sorted map of lower similarity bound and cumulative count, e.g., {0.97=59, 0.95=91, ...}
   */
  public SortedMap<Double, Integer> getCumulativeCountsBySimilarity(ObjectId projectId) {
    List<Document> output = new ArrayList<>();
    final long numberOfBuckets = 100L;
    final String MIN_SIMILARITY = "min";
    final String CUMULATIVE_COUNT = "cumSum";
    mongoTemplate.getCollection(mongoTemplate.getCollectionName(MongoRecordPair.class))
      .aggregate(Arrays.asList(
        new Document("$match", new Document("projectId", projectId)),
        new Document(
          "$bucketAuto", new Document("groupBy", "$similarity").append("buckets", numberOfBuckets)),
        new Document(
          "$setWindowFields",
          new Document("sortBy", new Document("_id.min", -1L))
            .append("output", new Document(
              CUMULATIVE_COUNT, new Document("$sum", "$count")
              .append("window", new Document("documents", Arrays.asList("unbounded", "current")))))
        ),
        new Document(
          "$project",
          new Document("_id", 0L)
            .append(MIN_SIMILARITY, "$_id.min")
            .append(CUMULATIVE_COUNT, "$cumSum")
        )
      )).forEach(output::add);
    SortedMap<Double, Integer> cumulativeCounts = new TreeMap<>(
      Comparator.comparing(Double::doubleValue).reversed());
    output.forEach(d -> cumulativeCounts.put(d.getDouble(MIN_SIMILARITY), d.getInteger(CUMULATIVE_COUNT)));
    return cumulativeCounts;
  }

  public void cleanRecordPairs(ObjectId projectId) {
    Optional<String> configValue =
      getProject(projectId).getConfigValue(CONFIG_RECORD_PAIR_LIMIT);
    configValue.ifPresent(pairLimit -> keepOnlyMostSimilarPairs(projectId, Integer.parseInt(pairLimit)));
  }

  public void keepOnlyMostSimilarPairs(ObjectId projectId, int maxPairCount) {
    SortedMap<Double, Integer> cumulativeCountsBySimilarity = getCumulativeCountsBySimilarity(projectId);
    double similarityBoundary = 0.0;
    for (Map.Entry<Double, Integer> doubleIntegerEntry : cumulativeCountsBySimilarity.entrySet()) {
      if (doubleIntegerEntry.getValue() > maxPairCount) {
        similarityBoundary = doubleIntegerEntry.getKey();
        break;
      }
    }
    deleteRecordPairsWithSimilarityLowerThan(projectId, similarityBoundary);
  }

  public void deleteRecordPairsWithSimilarityLowerThan(ObjectId projectId, double minSimilarity) {
    long count = recordPairRepository.countByProjectId(projectId);
    recordPairRepository.deleteByProjectIdAndSimilarityLessThan(projectId, minSimilarity);
    long countAfter = recordPairRepository.countByProjectId(projectId);
    log.info("Deleted {} record pairs with similarity lower than {} (before: {}, after: {})",
      count - countAfter, minSimilarity, count, countAfter
    );
  }

  /**
   * Add improved links to the database (+merge info from old ones). The old record pairs are deactivated.
   */
  public void mergeNewImprovedRecordPairs(ObjectId projectId,
    Collection<MongoRecordPair> updatedRecordPairs) {
    int previousImprovedLinksCount =
      getRecordPairsFilteredByProperties(projectId, Set.of(PROPERTY_IMPROVED_LINK)).size();
    int newImprovedLinksCount = previousImprovedLinksCount + updatedRecordPairs.size();
    log.debug("New improved links count: {} (previous: {}, added: {})", newImprovedLinksCount,
      previousImprovedLinksCount, updatedRecordPairs.size()
    );

    for (MongoRecordPair rp : updatedRecordPairs) {
      rp.addProperty(LinkageProcessDataSet.NEW);
      rp.addTag(Tag.create(
        TAG_IMPROVED_LINK_COUNT, String.valueOf(newImprovedLinksCount), (double) newImprovedLinksCount));
//        mergedPair.getProperties().remove(LinkageProcessDataSet.TAG_REMOVED_BY_CLASSIFIER);
//        mergedPair.getProperties().remove(LinkageProcessDataSet.TAG_REMOVED_BY_POSTPROCESSING);
    }
    mergeUpdatedRecordPairs(projectId, updatedRecordPairs);
  }

  /**
   * Add updated copies of the given record pairs to the database. The old record pairs are deactivated.
   * The new pairs are created by replacing the classification and tags, and merging the properties.
   * Properties of updated pairs that begin with "!" are stripped of this prefix and removed.
   */
  public void mergeUpdatedRecordPairs(ObjectId projectId, Collection<MongoRecordPair> updatedRecordPairs) {
    Map<String, MongoRecordPair> updatedPairsByPairId = updatedRecordPairs.stream()
      .collect(Collectors.toMap(MongoRecordPair::getPairId, Function.identity()));
    List<MongoRecordPair> oldPairs = retrieveExistingPairsByPairId(projectId, updatedRecordPairs);
    log.debug("Merging data from updated record pairs");
    List<MongoRecordPair> newMergedRecordPairs = oldPairs.parallelStream()
//      .map(MongoRecordPair::duplicate) // Unnecessary!?!
      .map(oldPair -> {
        MongoRecordPair mergedPair = oldPair.duplicate();
        MongoRecordPair updatedPair = updatedPairsByPairId.get(mergedPair.getPairId());
        mergedPair.setClassification(updatedPair.getClassification());
        mergedPair.getTags().clear();
        updatedPair.getTags().forEach(mergedPair::addTag);
        mergedPair.setProperties(new HashSet<>(updatedPair.getProperties()));
        for (String property : mergedPair.getProperties()) {
          if (property.startsWith("!")) {
            String propertyToRemove = property.substring(1); // Extract the property name after "!"
            mergedPair.getProperties().remove(property);
            mergedPair.getProperties().remove(propertyToRemove);
          }
        }
        if (updatedPair.getAttributeSimilarities().isPresent()) {
          mergedPair.setAttributeSimilarities(updatedPair.getAttributeSimilarities().get());
        }
        if (mergedPair.getClassification().isAtMost(MatchGrade.POSSIBLE_MATCH)) {
          mergedPair.addTag(TAG_REMOVED_BY_CLASSIFIER);
        }
//        mergedPair.removeProperty(PROPERTY_LINK_FROM_UPPER_LAYER);
        return mergedPair;
      })
      .peek(DatabaseLinkageProcessDataset::updateActiveProperty)
      .collect(Collectors.toList());
    persistOldAndNewPairs(projectId, newMergedRecordPairs, oldPairs);
  }

  /**
   * Deactivate old pairs and add new pairs.
   */
  public void replaceRecordPairs(ObjectId projectId, Collection<MongoRecordPair> newRecordPairs) {
    List<MongoRecordPair> oldPairs = retrieveExistingPairsByPairId(projectId, newRecordPairs);
    persistOldAndNewPairs(projectId, newRecordPairs, oldPairs);
  }

  private List<MongoRecordPair> retrieveExistingPairsByPairId(ObjectId projectId,
    Collection<MongoRecordPair> recordPairs) {
    return removeReplacedRecordPairs(recordPairRepository.findMongoRecordPairByProjectIdAndPairIdIn(
      projectId,
      recordPairs.stream()
        .map(MongoRecordPair::getPairId)
        .collect(Collectors.toList())
    ));
  }

  private void persistOldAndNewPairs(ObjectId projectId, Collection<MongoRecordPair> newRecordPairs,
    List<MongoRecordPair> oldPairs) {
    log.info("Deactivating {} replaced record pairs", oldPairs.size());
    oldPairs.forEach(oldPair -> {
      oldPair.removeProperty(ACTIVE);
      oldPair.addProperty(REPLACED);
    });
    recordPairRepository.saveAll(oldPairs);
    log.info("Inserting {} new record pairs", newRecordPairs.size());
    newRecordPairs.forEach(rp -> {
      rp.setProjectId(projectId);
      rp.removeProperty(PROPERTY_LINK_FROM_UPPER_LAYER);
      rp.set_id(null);
    });
    recordPairRepository.saveAll(newRecordPairs);
  }

  public void replaceRecordPair(ObjectId projectId, MongoRecordPair newRecordPair) {
    Optional<MongoRecordPair> optionalOldPair =
      recordPairRepository.findMongoRecordPairByProjectIdAndPairId(projectId, newRecordPair.getPairId());
    optionalOldPair.ifPresent(oldRecordPair -> {
      oldRecordPair.removeProperty(ACTIVE);
      recordPairRepository.save(oldRecordPair);
    });
    newRecordPair.setProjectId(projectId);
    recordPairRepository.save(newRecordPair);
  }

  public long getRecordPairCount(ObjectId projectId) {
    return recordPairRepository.countByProjectId(projectId);
  }

  public Collection<RecordPair> getRecordPairsWithIdsOnly(ObjectId projectId) {
    return recordPairRepository.findMongoRecordPairByProjectId(projectId);
  }

  public List<RecordPair> addGroundTruthTag(ObjectId projectId, List<RecordPair> pairs) {
    int datasetId = getProject(projectId).getDatasetId();
    Optional<MongoGroundTruth> groundTruth = datasetService.getGroundTruth(datasetId);
    if (groundTruth.isPresent()) {
      log.info("Adding ground truth tag to {} pairs", pairs.size());
      EvaluationUtils.addGroundTruthTags(pairs, groundTruth.get().getGroundTruth());
    }
    return pairs;
  }

  public List<RecordPair> getAllRecordPairs(ObjectId projectId) {
    return recordPairRepository.findMongoRecordPairByProjectId(projectId);
  }

  public List<RecordPair> getRecordPairs(ObjectId projectId) {
    return removeReplacedRecordPairs(getAllRecordPairs(projectId));
  }

  public List<RecordPair> getRecordPairsNoRecords(ObjectId projectId) {
    return removeReplacedRecordPairs(recordPairRepository.findMongoRecordPairByProjectIdNoRecords(projectId));
  }

  public List<RecordPair> getRecordPairsFilteredByProperties(ObjectId projectId, Set<String> properties) {
    if (properties != null && properties.contains("ALL")) {
      HashSet<String> newProperties = new HashSet<>(properties);
      newProperties.remove("ALL");
      if (newProperties.isEmpty()) {
        return getAllRecordPairs(projectId);
      }
      return recordPairRepository.findMongoRecordPairByProjectIdAndPropertiesAll(projectId, newProperties);
    }
    return removeReplacedRecordPairs(
      recordPairRepository.findMongoRecordPairByProjectIdAndPropertiesAll(projectId, properties)
    );
  }

  public List<RecordPair> getClassifiedRecordPairs(ObjectId projectId) {
    return removeReplacedRecordPairs(recordPairRepository.findMongoRecordPairByProjectIdAndClassificationIsIn(
      projectId, List.of(MatchGrade.CERTAIN_MATCH, MatchGrade.PROBABLE_MATCH, MatchGrade.POSSIBLE_MATCH,
        MatchGrade.NON_MATCH
      )));
  }

  public static <T extends RecordPair> List<T> removeReplacedRecordPairs(Collection<T> recordPairs) {
    return recordPairs.stream()
      .filter(rp -> !((MongoRecordPair) rp).getProperties().contains(LinkageProcessDataSet.REPLACED))
      .collect(Collectors.toList());
  }

  public DatasetMongoService getDatasetService() {
    return datasetService;
  }

  public void deleteAllProjects() {
    log.info("Deleting all projects");
    batchMatchProjectRepository.findAll().forEach(p -> deleteProject(p.getProjectId()));
  }

  public void deleteProject(ObjectId projectId) {
    log.info("Deleting project {}", projectId);
    recordPairRepository.deleteByProjectId(projectId);
    clusterRepository.deleteByProjectId(projectId);
    batchMatchProjectRepository.deleteById(projectId);
    datasetService.deleteClusters(projectId);
  }

  public void resetRecordPairs(ObjectId projectId) {
    Query query = new Query(Criteria.where("projectId").is(projectId));
    Update update = new Update();
    update.set("classification", MatchGrade.UNKNOWN);
    update.set("similarity", -1);
    update.set("attributeSimilarities", null);
    update.set("properties", ACTIVE);
    update.set("tags", null);
    runRecordPairUpdateQuery(query, update);
  }

  public void resetUncertaintyPropertyOfAllPairs(ObjectId projectId) {
    Query query = new Query(Criteria.where("projectId").is(projectId));
    Update update = new Update();
    update.pull("properties", LinkImprovementService.PROPERTY_UNCERTAIN_LINK);
    runRecordPairUpdateQuery(query, update);
  }

  public void runRecordPairUpdateQuery(Query query, Update update) {
    mongoTemplate.updateMulti(query, update, MongoRecordPair.class);
  }
}

package de.unileipzig.dbs.pprl.service.linkageunit.services;

import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import de.unileipzig.dbs.pprl.core.matcher.matcher.DatasetBasedBatchMatcher;
import de.unileipzig.dbs.pprl.core.matcher.matcher.DefaultBatchMatcher;
import de.unileipzig.dbs.pprl.core.matcher.matcher.Matcher;
import de.unileipzig.dbs.pprl.core.matcher.matcher.TwoStepBatchMatcher;
import de.unileipzig.dbs.pprl.core.matcher.model.api.LinkageProcessDataSet;
import de.unileipzig.dbs.pprl.service.common.data.converter.MongoRecordConverter;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordEncodingWishDto;
import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoRecord;
import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoRecordPair;
import de.unileipzig.dbs.pprl.service.linkageunit.config.MatcherConfig;
import de.unileipzig.dbs.pprl.service.linkageunit.data.converter.RecordPairDtoConverter;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.MatcherIdDto;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.RecordPairDto;
import de.unileipzig.dbs.pprl.service.linkageunit.data.mongo.BatchMatchProject;
import de.unileipzig.dbs.pprl.service.linkageunit.data.mongo.ProjectState;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static de.unileipzig.dbs.pprl.core.matcher.model.api.LinkageProcessDataSet.NEW;

@Service
@Slf4j
public class AsynchronousBatchMatcherService extends AbstractMatcherService {

  private final ProjectService projectService;

  @Getter
  private final LinkImprovementService linkImprovementService;

  public AsynchronousBatchMatcherService(MatcherConfig config,
    MatcherProviderService matcherProviderService,
    ProjectService projectService, LinkImprovementService linkImprovementService) {
    super(new MongoRecordConverter(), config, matcherProviderService);
    this.projectService = projectService;
    this.linkImprovementService = linkImprovementService;
  }

  public DatasetBasedBatchMatcher getMatcher(BatchMatchProject project) {
    MatcherIdDto matcherId = MatcherIdDto.builder().method(project.getMethod()).build();
    Matcher matcher = matcherProviderService.getMatcher(matcherId);
    DatasetBasedBatchMatcher datasetBasedBatchMatcher;
    if (matcher instanceof DatasetBasedBatchMatcher) {
      datasetBasedBatchMatcher = (DatasetBasedBatchMatcher) matcher;
    } else if (matcher instanceof DefaultBatchMatcher) {
      log.info("Converting DefaultBatchMatcher to DatasetBasedBatchMatcher");
      DefaultBatchMatcher defaultBatchMatcher = (DefaultBatchMatcher) matcher;
      datasetBasedBatchMatcher = new DatasetBasedBatchMatcher(
        defaultBatchMatcher.getBlocker(),
        defaultBatchMatcher.getLinker(),
        defaultBatchMatcher.getClusterer()
      );
      if (matcher instanceof TwoStepBatchMatcher) {
        TwoStepBatchMatcher twoStepBatchMatcher = (TwoStepBatchMatcher) matcher;
        datasetBasedBatchMatcher.setLinksPostprocessor(twoStepBatchMatcher.getLinksPostprocessor());
      } else {
        defaultBatchMatcher.setLinksPostprocessor(defaultBatchMatcher.getLinksPostprocessor());
      }
    } else {
      throw new IllegalArgumentException("Matcher is not a DatasetBasedBatchMatcher");
    }
    datasetBasedBatchMatcher.setDataSet(projectService.getDataset(project.getProjectId()));
    return datasetBasedBatchMatcher;
  }

//  public BatchMatchProject reset(ObjectId projectId, ProjectState targetState) {
//    BatchMatchProject project = projectService.getProject(projectId);
//    if (project.getState().isAtMost(targetState)) {
//      log.warn("Cannot reset to state {} from state {}", targetState, project.getState());
//      return project;
//    }
//    log.info("Resetting project {} to state {}", projectId, targetState);
//    project.setState(targetState);
//    switch (targetState) {
//      case COLLECTING:
//        projectService.deleteRecordPairs(projectId);
//        break;
//      case BLOCKING:
////        log.info("Fetching record pairs");
////        List<RecordPair> recordPairs = projectService.getRecordPairs(projectId);
//        log.info("Resetting record pairs");
//        projectService.resetRecordPairs(projectId);
//        log.info("Updated record pairs");
////        List<MongoRecordPair> pairs = recordPairs.parallelStream()
////          .map(rp -> {
////            MongoRecordPair mrp = (MongoRecordPair) rp;
////            mrp.setSimilarity(-1);
////            mrp.setClassification(MatchGrade.UNKNOWN);
////            mrp.setAttributeSimilarities(null);
////            mrp.removeTag(WekaClassifier.TAG_PROBABILITY);
//////            mrp.removeTag(DefaultLinker.TAG_MATCH_GRADE);
////            mrp.addProperty(ACTIVE);
////            return mrp;
////          })
////          .collect(Collectors.toList());
////        log.info("Updating {} record pairs", pairs.size());
////        projectService.addRecordPairs(projectId, pairs);
//      case CLASSIFICATION:
//        break;
//    }
//    projectService.save(project);
//    return project;
//  }

  public BatchMatchProject runTo(ObjectId projectId, ProjectState targetState) {
    while (targetState != null && !projectService.getProject(projectId).getState().isAtLeast(targetState)) {
      runNext(projectId);
    }
    return projectService.getProject(projectId);
  }

  public BatchMatchProject runNext(ObjectId projectId) {
    BatchMatchProject project = projectService.getProject(projectId);
    if (project.isInteractive()) {
      switch (project.getState()) {
        case COLLECTING:
          log.info("Running blocking, linking and classification for project {} on dataset {}", projectId,
            project.getDatasetId()
          );
          getMatcher(project).runBlockedLinking();
          //TODO Add reports based on runtime metrics, e.g. block sizes, average comparison times, etc.
          project.setState(ProjectState.CLASSIFICATION);
          break;
        case BLOCKING:
          log.info("Running linking and classification");
          getMatcher(project).compareAndClassifyActiveRecordPairs();
          project.setState(ProjectState.CLASSIFICATION);
          break;
        case LINKING:
//          log.error("Running classification alone is not supported yet");
          getMatcher(project).reclassifyRecordPairs();
          break;
        case CLASSIFICATION:
          log.info("Running postprocessing");
          getMatcher(project).runPostProcessing();
          project.setState(ProjectState.POSTPROCESSING);
          determineUncertainLinks(project.getProjectId(), Integer.MAX_VALUE);
          createEncodingWishesForUncertainLinks(project.getProjectId());
          break;
        case POSTPROCESSING:
          log.info("Running clustering");
          getMatcher(project).runClustering();
          project.setState(ProjectState.CLUSTERING);
          break;
        case CLUSTERING:
          break;
      }
    } else {
      log.info("Running all project phases");
      getMatcher(project).runAll();
    }
    projectService.save(project);
    return project;
  }

  public void runForNewRecords(ObjectId projectId) {
    BatchMatchProject project = projectService.getProject(projectId);
    if (!project.getState().isAtMost(ProjectState.CLASSIFICATION)) {
      log.warn("Running for new records is currently only possible before postprocessing");
      return;
    }
    Collection<Record> newRecords =
      projectService.getDatasetService().getAllRecords(project.getDatasetId()).stream()
        .map(record -> (MongoRecord) record)
        .filter(record -> record.getProperties().contains(NEW))
        .peek(record -> record.getProperties().remove(NEW))
        .collect(Collectors.toList());
    log.info("Comparing {} new records", newRecords.size());
    getMatcher(project).runBlockedLinking(newRecords);
    Collection<MongoRecord> mongoRecords = newRecords.stream()
      .map(record -> (MongoRecord) record)
      .collect(Collectors.toList());
    projectService.getDatasetService().addRecords(project.getDatasetId(), mongoRecords);
    project.setState(ProjectState.CLASSIFICATION);
    projectService.save(project);
  }

  public void compareNewPairs(ObjectId projectId) {
    BatchMatchProject project = projectService.getProject(projectId);
    if (!project.getState().isAtMost(ProjectState.CLASSIFICATION)) {
      log.warn("Running for new pairs is currently only possible before postprocessing");
      return;
    }

    Collection<RecordPair> newPairs =
      projectService.getRecordPairsFilteredByProperties(projectId, Set.of(LinkageProcessDataSet.NEW))
        .stream()
        .map(pair -> (MongoRecordPair) pair)
//        .peek(pair -> pair.getProperties().remove(NEW))
        .collect(Collectors.toList());

    log.info("Comparing {} new record pairs", newPairs.size());
    getMatcher(project).compareRecordPairs(newPairs);
    project.setState(ProjectState.LINKING);
    projectService.save(project);
  }

  public List<RecordPairDto> determineUncertainLinks(ObjectId projectId, int limit) {
    return linkImprovementService.determineUncertainLinks(projectId, limit).parallelStream()
      .map(RecordPairDtoConverter::convertRecordPairToDto)
      .toList();
  }

  public void markUncertainLinks(ObjectId projectId, List<MongoRecordPair> uncertainLinks) {
    linkImprovementService.markUncertainLinks(projectId, uncertainLinks);
  }

  public List<RecordEncodingWishDto> createEncodingWishesForUncertainLinks(ObjectId projectId) {
    List<MongoRecordPair> uncertainLinks = projectService.getRecordPairsFilteredByProperties(
        projectId, Set.of(LinkImprovementService.PROPERTY_UNCERTAIN_LINK)
      ).parallelStream()
      .map(rp -> (MongoRecordPair) rp)
      .toList();
    linkImprovementService.deleteEncodingWishes(projectId);
    return linkImprovementService.createEncodingWishesForUncertainLinks(projectId, uncertainLinks);
  }

}

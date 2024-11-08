package de.unileipzig.dbs.pprl.service.linkageunit.services.reporting;

import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import de.unileipzig.dbs.pprl.service.common.data.dto.reporting.ReportGroup;
import de.unileipzig.dbs.pprl.service.common.services.DatasetMongoService;
import de.unileipzig.dbs.pprl.service.linkageunit.config.LinkageUnitReportingConfig;
import de.unileipzig.dbs.pprl.service.linkageunit.data.mongo.BatchMatchProject;
import de.unileipzig.dbs.pprl.service.linkageunit.data.mongo.BatchMatchProjectPhase;
import de.unileipzig.dbs.pprl.service.linkageunit.data.mongo.ProjectState;
import de.unileipzig.dbs.pprl.service.linkageunit.services.ProjectService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ReportUpdater {
  public static final String SIZE = "Size";

  private final DatasetMongoService datasetService;

  private ProjectService projectService;

  @Getter
  private final LinkageUnitReportingConfig reportingConfig;

  public ReportUpdater(DatasetMongoService datasetService, LinkageUnitReportingConfig reportingConfig) {
    this.datasetService = datasetService;
    this.reportingConfig = reportingConfig;
  }

  public void setProjectService(ProjectService projectService) {
    this.projectService = projectService;
  }

  public void updateProjectPhases(BatchMatchProject batchMatchProject) {
    log.info("Updating project phases for project {}", batchMatchProject.getProjectId());
    if (batchMatchProject.getState().equals(ProjectState.COLLECTING)) {
      log.debug("Updating project phase {}", ProjectState.COLLECTING);
      BatchMatchProjectPhase phase =
        BatchMatchProjectPhase.builder()
          .reportGroup(DatasetReporting.DATASET_PROPERTIES,
            new DatasetReporting(datasetService).createReportOnDataset(batchMatchProject.getDatasetId())
          )
          .build();
      batchMatchProject.setPhase(batchMatchProject.getState().name(), phase);
    }
    if (batchMatchProject.getState().isAtLeast(ProjectState.BLOCKING)) {
      if (reportingConfig.isSkipBlockingReports()) {
        log.debug("Skip reporting on blocking, as configured");
      } else {
        log.debug("Updating project phase {}", ProjectState.BLOCKING);
        BatchMatchProjectPhase.BatchMatchProjectPhaseBuilder batchMatchProjectPhaseBuilder =
          BatchMatchProjectPhase.builder();
        datasetService.getGroundTruth(batchMatchProject.getDatasetId()).ifPresent(gt ->
          batchMatchProjectPhaseBuilder.reportGroup(BlockingQualityReporting.REPORT_GROUP_NAME,
            BlockingQualityReporting.createReportGroup(
              projectService.getDatasetService().getClusters(batchMatchProject.getProjectId()),
              gt.getGroundTruth()
            )
          ));
        BatchMatchProjectPhase phase = batchMatchProjectPhaseBuilder.build();
        batchMatchProject.setPhase(ProjectState.BLOCKING.name(), phase);
      }
    }
    if (batchMatchProject.getState().isAtLeast(ProjectState.CLASSIFICATION)) {
      log.debug("Updating project phase {}", batchMatchProject.getState());
      List<RecordPair> recordPairs = projectService.getAllRecordPairs(batchMatchProject.getProjectId());
      BatchMatchProjectPhase.BatchMatchProjectPhaseBuilder batchMatchProjectPhaseBuilder =
        BatchMatchProjectPhase.builder()
          .reportGroup(RecordPairsReporting.REPORT_GROUP_NAME,
            RecordPairsReporting.createReportFromRecordPairs(recordPairs)
          );
      datasetService.getGroundTruth(batchMatchProject.getDatasetId()).ifPresent(gt -> {
          ReportGroup previousReportGroup = null;
          try {
            previousReportGroup = batchMatchProject.getPhases().get(ProjectState.CLASSIFICATION.name()).getReportGroups()
              .get(QualityEvaluationReporting.REPORT_GROUP_NAME);
          } catch (Exception e) {
            log.debug("No previous report group found for project {}", batchMatchProject.getProjectId());
          }
          batchMatchProjectPhaseBuilder.reportGroup(QualityEvaluationReporting.REPORT_GROUP_NAME,
            QualityEvaluationReporting.createReportGroup(
              recordPairs,
              gt.getGroundTruth(),
              previousReportGroup
              ));
        });
      BatchMatchProjectPhase phase = batchMatchProjectPhaseBuilder.build();
      batchMatchProject.setPhase(ProjectState.CLASSIFICATION.name(), phase);
    }
    projectService.save(batchMatchProject);
  }
}

package de.unileipzig.dbs.pprl.service.linkageunit.services.reporting;

import de.unileipzig.dbs.pprl.service.common.services.DatasetMongoService;
import de.unileipzig.dbs.pprl.service.common.data.dto.reporting.Report;
import de.unileipzig.dbs.pprl.service.common.data.dto.reporting.ReportGroup;
import de.unileipzig.dbs.pprl.service.common.data.dto.reporting.ReportType;

public class DatasetReporting {

  public static final String DATASET_PROPERTIES = "Dataset properties";
  public static final String SIZE = "Size";
  private DatasetMongoService datasetMongoService;

  public DatasetReporting(DatasetMongoService datasetMongoService) {
    this.datasetMongoService = datasetMongoService;
  }

  public ReportGroup createReportOnDataset(int datasetId) {
    return ReportGroup.builder().name(DATASET_PROPERTIES).report(Report.builder()
        .name(SIZE)
        .type(ReportType.TEXT)
        .report(String.valueOf(datasetMongoService.size(datasetId)))
        .build()
    ).build();
  }
}

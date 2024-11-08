package de.unileipzig.dbs.pprl.service.common.services;

import de.unileipzig.dbs.pprl.core.analyzer.AnalysisResult;
import de.unileipzig.dbs.pprl.core.analyzer.DataSetAnalyzer;
import de.unileipzig.dbs.pprl.core.analyzer.DataSetAnalyzerCreator;
import de.unileipzig.dbs.pprl.core.analyzer.attribute.AttributeAvailability;
import de.unileipzig.dbs.pprl.core.analyzer.attribute.AttributeLength;
import de.unileipzig.dbs.pprl.core.analyzer.attribute.AttributeMostFrequent;
import de.unileipzig.dbs.pprl.core.analyzer.attribute.AttributePatternFrequency;
import de.unileipzig.dbs.pprl.core.analyzer.results.ResultSet;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.impl.PersonalAttributeType;
import de.unileipzig.dbs.pprl.core.common.validation.impl.DetailedValidationResult;
import de.unileipzig.dbs.pprl.core.common.validation.impl.FieldErrorCode;
import de.unileipzig.dbs.pprl.service.common.config.ReportingConfig;
import de.unileipzig.dbs.pprl.service.common.config.SourceConfig;
import de.unileipzig.dbs.pprl.service.common.data.converter.RecordConverter;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordRequirementsDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.analysis.AnalysisRequestDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.analysis.AnalysisResultDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.reporting.Report;
import de.unileipzig.dbs.pprl.service.common.data.dto.reporting.ReportGroup;
import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoAnalysisResult;
import de.unileipzig.dbs.pprl.service.common.persistence.repositories.mongo.MongoAnalysisResultRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.tablesaw.api.BooleanColumn;
import tech.tablesaw.api.LongColumn;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Provides descriptions of a dataset that can be used for
 * - reports on the data quality
 * - selection and parametrization of the linkage / encoding method
 */
@Service
@Slf4j
public class AnalysisService {

  private final SourceConfig sourceConfig;

  private final ReportingConfig reportingConfig;

  private final DatasetDtoService datasetDtoService;

  private final ValidationService validationService;

  private static final RecordConverter converter = new RecordConverter();

  private final List<String> availableAnalysisTypes = new ArrayList<>();

  private final MongoAnalysisResultRepository analysisResultRepository;

  @PostConstruct
  private void init() {
    availableAnalysisTypes.add(MongoAnalysisResult.Type.VALIDATION.name());
    availableAnalysisTypes.add(MongoAnalysisResult.Type.DATASET_DESCRIPTION.name());
  }

  public AnalysisService(SourceConfig sourceConfig, ReportingConfig reportingConfig, DatasetDtoService datasetDtoService,
    ValidationService validationService, MongoAnalysisResultRepository analysisResultRepository) {
    this.sourceConfig = sourceConfig;
    this.reportingConfig = reportingConfig;
    this.datasetDtoService = datasetDtoService;
    this.validationService = validationService;
    this.analysisResultRepository = analysisResultRepository;
  }

  public List<String> getAvailableAnalysisTypes() {
    return availableAnalysisTypes;
  }

  public int deleteAnalysisResults(int datasetId) {
    Collection<MongoAnalysisResult> byDatasetId = analysisResultRepository.findByDatasetId(datasetId);
    log.info("Deleting " + byDatasetId.size() + " analysis results for dataset " + datasetId);
    analysisResultRepository.deleteAll(byDatasetId);
    return byDatasetId.size();
  }

  public AnalysisResultDto getAnalysisResult(AnalysisRequestDto requestDto) {
    log.info("Analysis requested: " + requestDto);
    MongoAnalysisResult.Type type = null;
    try {
      type = MongoAnalysisResult.Type.valueOf(requestDto.getType());
    } catch (IllegalArgumentException e) {
      throw new RuntimeException("Unknown analysis type: " + requestDto.getType() + ". " +
        "Available types: " + availableAnalysisTypes);
    }

    Optional<MongoAnalysisResult> byDatasetIdAndType =
      analysisResultRepository.findByDatasetIdAndSourceAndType(requestDto.getDatasetId(),
        sourceConfig.getName(), type
      );

    if (byDatasetIdAndType.isPresent()) {
      String shallRefresh = requestDto.getParameters().get("refresh");
      if (Boolean.parseBoolean(shallRefresh)) {
        analysisResultRepository.delete(byDatasetIdAndType.get());
      } else {
        log.info("Returning precomputed analysis result from the database");
        return byDatasetIdAndType.get().getResult();
      }
    }
    MongoAnalysisResult mongoAnalysisResult = runAnalysis(requestDto);
    mongoAnalysisResult.setSource(sourceConfig.getName());
    analysisResultRepository.save(mongoAnalysisResult);
    return mongoAnalysisResult.getResult();
  }

  public MongoAnalysisResult runAnalysis(AnalysisRequestDto requestDto) {
    String type = requestDto.getType();
    if (MongoAnalysisResult.Type.VALIDATION.name().equals(type)) {
      throw new RuntimeException("Error: Analysis of type " + MongoAnalysisResult.Type.VALIDATION.name()
        + " must be run via validateDataSet()");
    }

    if (MongoAnalysisResult.Type.DATASET_DESCRIPTION.name().equals(type)) {
      AnalysisResultDto analysisResultDto =
        runDataSetAnalyzer(requestDto.getDatasetId(), requestDto.getParameters());
      return MongoAnalysisResult.builder()
        .datasetId(requestDto.getDatasetId())
        .type(MongoAnalysisResult.Type.DATASET_DESCRIPTION)
        .result(analysisResultDto)
        .build();
    }
    throw new RuntimeException("Unknown analysis type: " + type);
  }

  public AnalysisResultDto validateDataSet(int idDataset, RecordRequirementsDto recordRequirements) {
    List<Record> records = getRecordsFromDatabase(idDataset);

    long total = records.size();
    long validWithReport = 0;
    long invalid = 0;

    Table validationRecords = Table.create(
      "VALIDATION_RECORDS",
      StringColumn.create("id"),
      BooleanColumn.create("isValid"),
      BooleanColumn.create("hasReport"),
      StringColumn.create("errorCode"),
      StringColumn.create("field"),
      StringColumn.create("message")
    );

    for (Record record : records) {
      DetailedValidationResult result =
        (DetailedValidationResult) validationService.validate(record, recordRequirements);
      if (result.isValid() && result.hasReport()) {
        validWithReport++;
      }
      if (!result.isValid()) {
        invalid++;
      }
      result.getEntries().stream()
        .filter(e -> e instanceof FieldErrorCode)
        .map(e -> (FieldErrorCode) e)
        .forEach(fec -> {
          Row row = validationRecords.appendRow();
          row.setString("id", record.getId().getUniqueId());
          row.setBoolean("isValid", result.isValid());
          row.setBoolean("hasReport", result.hasReport());
          row.setString("errorCode", fec.getErrorCode());
          row.setString("field", fec.getField());
          row.setString("message", fec.getMessage() == null ? "" : fec.getMessage());
        });
    }
    long valid = total - validWithReport - invalid;

    Table validationSummary = Table.create(
      "VALIDATION_SUMMARY",
      StringColumn.create("name", List.of("TOTAL", "VALID", "VALID_WITH_REPORT", "INVALID")),
      LongColumn.create("value", total, valid, validWithReport, invalid)
    );

    return AnalysisResultDto.builder()
      .name(MongoAnalysisResult.Type.VALIDATION.name())
      .reportGroup(ReportGroup.builder()
        .name(MongoAnalysisResult.Type.VALIDATION.name())
        .report(Report.createTableReport(validationSummary.name(), validationSummary))
        .report(Report.createTableReport(validationRecords.name(), validationRecords))
        .build()
      )
      .build();
  }

  public AnalysisResultDto runDataSetAnalyzer(int idDataset, Map<String, String> parameters) {
    List<Record> records = datasetDtoService.getRecordsWithGlobalIdIfAvailable(idDataset).stream()
      .map(converter::toRecord)
      .collect(Collectors.toList());
    DataSetAnalyzer dsa = DataSetAnalyzerCreator.createForDataSet(records);
    if (parameters.get("runPerSource") != null) {
      dsa.setRunPerSource(Boolean.parseBoolean(parameters.get("runPerSource")));
    }
    AnalysisResult analysisResult = dsa.run(records);
    return buildAnalysisResultDto(analysisResult);
  }

  public static AnalysisResultDto buildAnalysisResultDto(AnalysisResult analysisResult) {
    return buildAnalysisResultDto(analysisResult, new ReportingConfig());
  }

  public static AnalysisResultDto buildAnalysisResultDto(AnalysisResult analysisResult, ReportingConfig config) {
    AnalysisResultDto.AnalysisResultDtoBuilder resultBuilder = AnalysisResultDto.builder()
      .name(MongoAnalysisResult.Type.DATASET_DESCRIPTION.name());
    if (analysisResult != null) {
      for (String analysisResultName : analysisResult.getResults().keySet()) {
        List<ResultSet> resultSets = analysisResult.getResults().get(analysisResultName);
        ReportGroup reportGroup = buildReportGroup(resultSets, config);
        reportGroup.setName(analysisResultName);
        resultBuilder.reportGroup(reportGroup);
      }
    }
    return resultBuilder.build();
  }

  private static ReportGroup buildReportGroup(List<ResultSet> resultSets, ReportingConfig config) {
    ReportGroup.ReportGroupBuilder reportGroupBuilder = ReportGroup.builder();
    for (ResultSet resultSet : resultSets) {
      Report report = buildReport(resultSet);
      reportGroupBuilder.report(report);
      if (config.isIncludeAdditionalResultsByDefault()) {
        for (Map.Entry<String, Table> additionResult : resultSet.getAdditionalResults().entrySet()) {
          Report addReport = Report.createTableReport(
            buildAdditionalResultName(report.getName(), additionResult.getKey()),
            additionResult.getValue());
          reportGroupBuilder.report(addReport);
        }
      }
    }
    return reportGroupBuilder.build();
  }

  public static String buildAdditionalResultName(String resultName, String additionResultName) {
    return resultName + " >>> " + additionResultName;
  }

  private static Report buildReport(ResultSet resultSet) {
    Report report = Report.createTableReport(resultSet.getName(), resultSet.getAsTable(true));
    report.setReport(resultSet.getDescription());
    return report;
  }

  private DataSetAnalyzer buildSingleSourceDataSetAnalyzer(Map<String, String> parameters) {
    //TODO Use parameters to build custom DataSetAnalyzer
    DataSetAnalyzer dsa = new DataSetAnalyzer();
    dsa.setRunPerSource(false);

    dsa.addAnalyzer(new AttributeAvailability());
    dsa.addAnalyzer(new AttributeLength());
    dsa.addAnalyzer(new AttributeMostFrequent());
//		dsa.addAnalyzer(new AttributeMostFrequentNGrams(1));
//    dsa.addAnalyzer(new AttributeMostFrequentNGrams(2));
//    dsa.addAnalyzer(new AttributeMostFrequentNGrams(3));

    AttributePatternFrequency apf = new AttributePatternFrequency();
    apf.addPatterns(PersonalAttributeType.FIRSTNAME.asString(), List.of(".*-.*", ".\\.", ".*\\s.*"));
    apf.addPatterns(PersonalAttributeType.LASTNAME.asString(), List.of(".*-.*", ".\\.", ".*\\s.*"));
    dsa.addAnalyzer(apf);
    return dsa;
  }

  private List<Record> getRecordsFromDatabase(int idDataset) {
    return datasetDtoService.getAllRecordsAsDto(idDataset).stream()
      .map(converter::toRecord)
      .collect(Collectors.toList());
  }
}

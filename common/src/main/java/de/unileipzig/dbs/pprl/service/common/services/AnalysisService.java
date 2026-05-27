package de.unileipzig.dbs.pprl.service.common.services;

import de.unileipzig.dbs.pprl.core.analyzer.AnalysisResult;
import de.unileipzig.dbs.pprl.core.analyzer.DataSetAnalyzer;
import de.unileipzig.dbs.pprl.core.analyzer.DataSetAnalyzerCreator;
import de.unileipzig.dbs.pprl.core.analyzer.attribute.AttributeAvailability;
import de.unileipzig.dbs.pprl.core.analyzer.attribute.AttributeLength;
import de.unileipzig.dbs.pprl.core.analyzer.attribute.AttributeMostFrequent;
import de.unileipzig.dbs.pprl.core.analyzer.attribute.AttributePatternFrequency;
import de.unileipzig.dbs.pprl.core.analyzer.results.ResultSet;
import de.unileipzig.dbs.pprl.core.analyzer.tags.record.BitVectorRecordAnalyzer;
import de.unileipzig.dbs.pprl.core.analyzer.tags.record.PlainRecordAnalyzer;
import de.unileipzig.dbs.pprl.core.analyzer.tags.record.RecordAnalyzer;
import de.unileipzig.dbs.pprl.core.analyzer.tags.recordpair.PlainRecordPairAnalyzer;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import de.unileipzig.dbs.pprl.core.common.model.impl.PersonalAttributeType;
import de.unileipzig.dbs.pprl.core.common.model.impl.RecordPairSimple;
import de.unileipzig.dbs.pprl.core.common.monitoring.Tag;
import de.unileipzig.dbs.pprl.core.common.monitoring.TagTableSerialization;
import de.unileipzig.dbs.pprl.core.common.validation.impl.DetailedValidationResult;
import de.unileipzig.dbs.pprl.core.common.validation.impl.FieldErrorCode;
import de.unileipzig.dbs.pprl.service.common.config.ReportingConfig;
import de.unileipzig.dbs.pprl.service.common.config.SourceConfig;
import de.unileipzig.dbs.pprl.service.common.data.converter.RecordConverter;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordIdPairDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordRequirementsDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.analysis.AnalysisRequestDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.analysis.AnalysisResultDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.reporting.Report;
import de.unileipzig.dbs.pprl.service.common.data.dto.reporting.ReportGroup;
import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoAnalysisResult;
import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoTag;
import de.unileipzig.dbs.pprl.service.common.dataset.MongoAttributesFrequencyLookupProvider;
import de.unileipzig.dbs.pprl.service.common.persistence.repositories.mongo.MongoAnalysisResultRepository;
import de.unileipzig.dbs.pprl.service.common.persistence.repositories.mongo.MongoFrequencyLookupRepository;
import de.unileipzig.dbs.pprl.service.common.persistence.repositories.mongo.MongoTagRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.tablesaw.api.BooleanColumn;
import tech.tablesaw.api.LongColumn;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import java.util.*;
import java.util.stream.Collectors;

import static de.unileipzig.dbs.pprl.core.analyzer.DataSetAnalyzer.RECORD_GROUP_ALL;

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

  private final DatasetMongoService datasetMongoService;

  private final ValidationService validationService;

  private static final RecordConverter converter = new RecordConverter();

  private final List<String> availableAnalysisTypes = new ArrayList<>();

  private final MongoAnalysisResultRepository analysisResultRepository;

  private final MongoTagRepository tagRepository;

  private MongoAttributesFrequencyLookupProvider frequencyLookupProvider;

  @PostConstruct
  private void init() {
    availableAnalysisTypes.add(MongoAnalysisResult.Type.VALIDATION.name());
    availableAnalysisTypes.add(MongoAnalysisResult.Type.DATASET_DESCRIPTION.name());
    availableAnalysisTypes.add(MongoAnalysisResult.Type.TAG_BASED_DATASET_ANALYSIS.name());
  }

  public AnalysisService(SourceConfig sourceConfig, ReportingConfig reportingConfig, DatasetDtoService datasetDtoService,
                         DatasetMongoService datasetMongoService, ValidationService validationService,
                         MongoAnalysisResultRepository analysisResultRepository, MongoTagRepository tagRepository,
                         MongoFrequencyLookupRepository mongoFrequencyLookupRepository) {
    this.sourceConfig = sourceConfig;
    this.reportingConfig = reportingConfig;
    this.datasetDtoService = datasetDtoService;
    this.datasetMongoService = datasetMongoService;
    this.validationService = validationService;
    this.analysisResultRepository = analysisResultRepository;
    this.tagRepository = tagRepository;
    this.frequencyLookupProvider = new MongoAttributesFrequencyLookupProvider(
            mongoFrequencyLookupRepository, datasetMongoService);
  }

  public List<String> getAvailableAnalysisTypes() {
    return availableAnalysisTypes;
  }

  public int deleteAnalysisResults(long datasetId) {
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
//    analysisResultRepository.save(mongoAnalysisResult);  # May exceed mongodb document size limit of 16MB
    return mongoAnalysisResult.getResult();
  }

  public void saveTags(long datasetId, Collection<Tag> tags) {
    log.info("Adding {} tags to dataset {}", tags.size(), datasetId);
    List<MongoTag> mongoTags = tags.stream().map(t -> MongoTag.create(datasetId, t))
            .toList();
    tagRepository.saveAll(mongoTags);
  }

  public void deleteTags(long datasetId) {
    log.info("Delete all tags from dataset {}", datasetId);
    tagRepository.deleteByDatasetId(datasetId);
  }

  public Collection<Tag> getTags(long datasetId, String origin) {
    Collection<MongoTag> tags;
    if (origin == null) {
      tags = tagRepository.findByDatasetId(datasetId);
    } else {
      tags = tagRepository.findByDatasetIdAndOrigin(datasetId, origin);
    }
    return tags.stream().map(MongoTag::getTag).collect(Collectors.toList());
  }

  public Collection<Tag> runPairAnalysis(long datasetId, List<RecordIdPairDto> idPairs) {
    log.info("Running pair analysis for dataset {} on {} record pairs.", datasetId, idPairs.size());
    List<Record> records = getRecordsFromDatabase(datasetId);
    Map<String, Record> recordsById = records.stream()
            .collect(Collectors.toMap(r -> r.getId().getUniqueLikeId(), r -> r));
    List<RecordPair> recordPairs = new ArrayList<>();
    for (RecordIdPairDto idPair : idPairs) {
      Record leftRecord = recordsById.get(idPair.getLeftRecordId().getUniqueLike());
      Record rightRecord = recordsById.get(idPair.getRightRecordId().getUniqueLike());
      if ((leftRecord == null) || (rightRecord == null)) {
        log.error("A record not found in: " + idPair);
        continue;
      }
      RecordPairSimple pair = new RecordPairSimple(leftRecord, rightRecord);
      recordPairs.add(pair);
    }

    PlainRecordPairAnalyzer analyzer = new PlainRecordPairAnalyzer();
    List<Tag> tags = recordPairs.stream()
            .flatMap(rp -> analyzer.getTags(rp).stream())
            .toList();
    return tags;
  }

  public MongoAnalysisResult runAnalysis(AnalysisRequestDto requestDto) {
    String type = requestDto.getType();
    if (MongoAnalysisResult.Type.VALIDATION.name().equals(type)) {
      throw new RuntimeException("Error: Analysis of type " + MongoAnalysisResult.Type.VALIDATION.name()
        + " must be run via validateDataSet()");
    }

    if (MongoAnalysisResult.Type.TAG_BASED_DATASET_ANALYSIS.name().equals(type)) {
      Collection<Tag> outputTags = new ArrayList<>();
      if (!requestDto.getParameters().containsKey("onlyStored")) {
        log.info("not: onlyStored");
        Optional<Collection<Tag>> tags = runTagBasedAnalysis(requestDto);
        tags.ifPresent(outputTags::addAll);
      }
      if (!requestDto.getParameters().containsKey("skipStored")) {
        log.info("not: skipStored");
        Collection<Tag> storedTags = getTags(requestDto.getDatasetId(), null);
        outputTags.addAll(storedTags);
      }
      log.info("Building AnalysisResultDto...");
      AnalysisResultDto analysisResultDto;
      if (outputTags.isEmpty()) {
        analysisResultDto = AnalysisResultDto.builder()
                .description("No tags have been generated by the analysis")
                .build();
      } else {
//                saveTags(requestDto.getDatasetId(), tags.get());
        analysisResultDto = AnalysisResultDto.builder()
                .name(MongoAnalysisResult.Type.TAG_BASED_DATASET_ANALYSIS.name())
                .description("Tags for dataset " + requestDto.getDatasetId())
                .reportGroup(ReportGroup.builder()
                        .name(RECORD_GROUP_ALL)
                        .report(Report.createTableReport(TagTableSerialization.convertToTable(outputTags))                        )
                        .build()
                )
                .build();
      }
      log.info("Finished building AnalysisResultDto");
      return MongoAnalysisResult.builder()
              .datasetId(requestDto.getDatasetId())
              .type(MongoAnalysisResult.Type.TAG_BASED_DATASET_ANALYSIS)
              .result(analysisResultDto)
              .build();
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

  public Optional<Collection<Tag>> runTagBasedAnalysis(AnalysisRequestDto analysisRequestDto) {
    if (analysisRequestDto.getDatasetId() > 0) {
      List<Tag> tags = new ArrayList<>();
      List<Record> records = getRecordsFromDatabase(analysisRequestDto.getDatasetId());
      RecordAnalyzer recordAnalyzer;
      if (DataSetAnalyzerCreator.isEncoded(records)) {
          recordAnalyzer = new BitVectorRecordAnalyzer();
      } else {
          recordAnalyzer = new PlainRecordAnalyzer();
          ((PlainRecordAnalyzer)recordAnalyzer).useAttributeFrequencyLookup(
                  frequencyLookupProvider.provide(analysisRequestDto.getDatasetId())
          );
      }
      records.forEach(r -> {
        List<Tag> recordTags = recordAnalyzer.getTags(r);
        tags.addAll(recordTags);
      });
      return Optional.of(tags);
    }
    return Optional.empty();
  }

  public AnalysisResultDto validateDataSet(long datasetId, RecordRequirementsDto recordRequirements) {
    List<Record> records = getRecordsFromDatabase(datasetId);

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

  public AnalysisResultDto runDataSetAnalyzer(long datasetId, Map<String, String> parameters) {
    List<Record> records = datasetDtoService.getRecordsWithGlobalIdIfAvailable(datasetId).stream()
      .map(converter::toRecord)
      .collect(Collectors.toList());
    DataSetAnalyzer dsa = DataSetAnalyzerCreator.createForDataSet(records);
    if (parameters.get("runPerSource") != null) {
      dsa.setRunPerSource(Boolean.parseBoolean(parameters.get("runPerSource")));
    }
    ReportingConfig reportingConfig = new ReportingConfig();
    if (parameters.get("includeAdditionalResults") != null) {
      reportingConfig.setIncludeAdditionalResultsByDefault(Boolean.parseBoolean(parameters.get("includeAdditionalResults")));
    }
    AnalysisResult analysisResult = dsa.run(records);
    return buildAnalysisResultDto(analysisResult, reportingConfig);
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
    return resultName + ">>>" + additionResultName;
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

  private List<Record> getRecordsFromDatabase(long datasetId) {
    return datasetDtoService.getAllRecordsAsDto(datasetId).stream()
      .map(converter::toRecord)
      .collect(Collectors.toList());
  }
}

package de.unileipzig.dbs.pprl.service.linkageunit.services;

import de.unileipzig.dbs.pprl.core.analyzer.AnalysisResult;
import de.unileipzig.dbs.pprl.core.analyzer.MatchResultAnalyzer;
import de.unileipzig.dbs.pprl.service.common.config.ReportingConfig;
import de.unileipzig.dbs.pprl.service.linkageunit.services.analysis.KaprAnalyzer;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import de.unileipzig.dbs.pprl.service.common.data.converter.RecordConverter;
import de.unileipzig.dbs.pprl.service.common.data.dto.analysis.AnalysisRequestDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.analysis.AnalysisResultDto;
import de.unileipzig.dbs.pprl.service.common.services.AnalysisService;
import de.unileipzig.dbs.pprl.service.linkageunit.data.converter.RecordPairDtoConverter;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.MatchResultAnalysisRequestDto;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.MatchResultDto;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LinkageResultAnalysisService {

  public static final String TYPE_SIMILARITY_DISTRIBUTION = "SIMILARITY_DISTRIBUTION";
  public static final String TYPE_PRIVACY = "PRIVACY_MEASURE";

  private final List<String> availableAnalysisTypes = new ArrayList<>();

  private final RecordConverter recordConverter = new RecordConverter();
  private final ProjectService projectService;

  public LinkageResultAnalysisService(ProjectService projectService) {
    this.projectService = projectService;
  }

  @PostConstruct
  private void init() {
    availableAnalysisTypes.add(TYPE_SIMILARITY_DISTRIBUTION);
    availableAnalysisTypes.add(TYPE_PRIVACY);
  }

  public List<String> getAvailableAnalysisTypes() {
    return availableAnalysisTypes;
  }

  public AnalysisResultDto runLinkResultAnalyzer(MatchResultAnalysisRequestDto requestDto) {
    AnalysisRequestDto analysisRequestDto = requestDto.getAnalysisRequest();
    log.info("Analysis requested: " + analysisRequestDto);

    List<RecordPair> recordPairs;
    if (requestDto.getMatchResult().getRecordPairs() == null) {
      final ObjectId prjId = parseProjectId(analysisRequestDto.getProjectId());
      recordPairs = projectService.getAllRecordPairs(prjId);
    } else {
      recordPairs = requestDto.getMatchResult().getRecordPairs().stream()
        .map(RecordPairDtoConverter::convertDtoToRecordPair)
        .collect(Collectors.toList());
    }
    AnalysisResult result = null;
    if (!recordPairs.isEmpty()) {
      MatchResultAnalyzer lra = buildLinkageResultAnalyzer(analysisRequestDto.getParameters(), requestDto.getMatchResult());
      result = lra.run(recordPairs);
    }
    ReportingConfig reportingConfig = new ReportingConfig();
    reportingConfig.setIncludeAdditionalResultsByDefault(true);
    return AnalysisService.buildAnalysisResultDto(result, reportingConfig);
  }

  private static ObjectId parseProjectId(String prjIdString) {
    try {
      return new ObjectId(prjIdString);
    } catch (Exception e) {
      throw new RuntimeException("Could not parse " + prjIdString + " as an ObjectId");
    }
  }

  private MatchResultAnalyzer buildLinkageResultAnalyzer(Map<String, String> parameters,
    MatchResultDto matchResultDto) {
    //TODO Use parameters to build custom LinkageResultAnalyzer
    List<Record> records = matchResultDto.getRecords().stream()
      .map(recordConverter::toRecord)
      .collect(Collectors.toList());
    MatchResultAnalyzer lra = new MatchResultAnalyzer();
//    lra.addAnalyzer(new SimilarityDistributionAnalyzer());
    lra.addAnalyzer(new KaprAnalyzer(parameters, records));
    return lra;
  }

}

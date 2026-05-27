package de.unileipzig.dbs.pprl.service.protocol.api;

import de.unileipzig.dbs.pprl.service.common.data.dto.RecordDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordEncodingWishDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.analysis.AnalysisRequestDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.analysis.AnalysisResultDto;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.*;
import de.unileipzig.dbs.pprl.service.linkageunit.data.mongo.ProjectState;
import kong.unirest.Body;
import kong.unirest.Unirest;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
public class MatcherApi extends CommonApi {

  public MatcherApi() {
    super("http://localhost:8082");
  }

  public BatchMatchProjectDto createProject(BatchMatchProjectDto request) {
    return Unirest.post(url + "/project/")
      .body(request)
      .asObject(BatchMatchProjectDto.class)
      .getBody();
  }

  public BatchMatchProjectDto updateProject(BatchMatchProjectDto project) {
    return Unirest.put(url + "/project/")
      .body(project)
      .asObject(BatchMatchProjectDto.class)
      .getBody();
  }

  public void deleteProject(String projectId, boolean deleteParents) {
    String endPoint = url + "/project/" + projectId;
    Unirest.delete(endPoint)
      .queryString("deleteParents", deleteParents)
      .asEmpty();
  }

  public MatchingDto trainMatcher(MatcherTrainingsRequest request) {
    return Unirest.post(url + "/evaluation/matcher/fit")
      .body(request)
      .asObject(MatchingDto.class)
      .getBody();
  }

  public MatchingDto updateMatcher(String projectId, MatcherUpdateType type) {
    return Unirest.post(url + "/protocol/matcher/update")
      .body(MatcherUpdateRequest.builder()
        .projectId(projectId)
        .type(type)
        .build())
      .asObject(MatchingDto.class)
      .getBody();
  }

  public List<RecordPairDto> determineUncertainLinks(String projectId) {
    RecordPairDto[] pairs = Unirest.get(url + "/protocol/pairs/uncertain/" + projectId)
      .asObject(RecordPairDto[].class)
      .getBody();
    return Arrays.asList(pairs);
  }

  public void reclassify(String projectId) {
    Unirest.post(url + "/protocol/pairs/reclassify/" + projectId)
      .asEmpty();
  }

  public List<RecordPairDto> getRecordPairs(String projectId, Set<String> properties) {
    log.debug("Fetching record pairs for project: " + projectId);
    RecordPairDto[] pairs = Unirest.post(url + "/analysis/pairs")
      .body(ResultRequest.builder()
        .projectId(projectId)
        .pairProperties(properties)
        .build()
      )
      .asObject(RecordPairDto[].class)
      .getBody();
    return Arrays.asList(pairs);
  }

  public void addRecordPairs(Collection<RecordPairDto> pairDtos, boolean merge) {
    Unirest.post(url + "/project/pairs")
      .body(pairDtos)
      .queryString("merge", merge)
      .asEmpty();
  }

  public BatchMatchProjectDto continueProject(String projectId) {
    return Unirest.post(url + "/project/run/" + projectId)
      .asObject(BatchMatchProjectDto.class)
      .getBody();
  }

  public BatchMatchProjectDto runProject(String projectId, ProjectState fromState, ProjectState toState) {
    return Unirest.post(url + "/project/run/")
      .body(BatchMatchProjectExecutionDto.builder()
        .projectId(projectId)
        .fromState(fromState)
        .toState(toState)
        .build()
      )
      .asObject(BatchMatchProjectDto.class)
      .getBody();
  }

  public BatchMatchProjectDto runProjectForNewRecords(String projectId) {
    return Unirest.post(url + "/project/runForNew/" + projectId)
      .asObject(BatchMatchProjectDto.class)
      .getBody();
  }

  public MatchingDto fetchMatching(String method) {
    return Unirest.post(url + "/config/findById")
      .body(MatcherIdDto.builder().method(method).build())
      .asObject(MatchingDto.class)
      .getBody();
  }

  public String fetchMatcherDescription(String method) {
    return Unirest.post(url + "/config/classifier")
      .body(MatcherIdDto.builder().method(method).build())
      .asString().getBody();
  }

  public void addMatching(MatchingDto matchingDto) {
    Unirest.post(url + "/config/")
      .body(matchingDto)
      .asEmpty();
  }

  public void updateMatching(MatchingDto matchingDto) {
    Unirest.put(url + "/config/")
      .body(matchingDto)
      .asEmpty();
  }

  public void addAndCompareRecordPairs(Collection<RecordPairDto> pairDtos) {
    Unirest.post(url + "/project/pairsComparison")
      .body(pairDtos)
      .asEmpty();
  }

  public void getResponse(String projectId) {
    Optional<Body> optionalBody = Unirest.get(url + "/project/" + projectId)
      .getBody();
    optionalBody.ifPresent(System.out::println);
  }

  public BatchMatchProjectDto getProject(String projectId) {
    return getProject(projectId, false);
  }

  public BatchMatchProjectDto getProject(String projectId, boolean update) {
    return Unirest.get(url + "/project/" + projectId)
      .queryString("update", update)
      .asObject(BatchMatchProjectDto.class)
      .getBody();
  }

  public List<RecordEncodingWishDto> getRecordEncodingWishes(String projectId, int wishLimit) {
//    log.debug("Fetching record encoding wishes...");
    RecordEncodingWishDto[] wishes = Unirest.get(url + "/protocol/wishlist/" + projectId)
      .queryString("limit", wishLimit)
      .asObject(RecordEncodingWishDto[].class)
      .getBody();
    log.debug("Fetched record encoding wishes: " + wishes.length);
    return Arrays.asList(wishes);
  }

  public int reportPairs(String projectId) {
    log.debug("Report record pairs...");
    return Unirest.post(url + "/protocol/pairs/report/" + projectId)
      .asObject(Integer.class)
      .getBody();
  }

  public void fetchPairs(String projectId) {
    log.debug("Fetch and compare uncertain pairs from parent project...");
    Unirest.post(url + "/protocol/pairs/fetch/" + projectId)
      .asEmpty();
  }

  public List<BatchMatchProjectDto> getProjects() {
    BatchMatchProjectDto[] projects = Unirest.get(url + "/project/findAll")
      .asObject(BatchMatchProjectDto[].class)
      .getBody();
    return Arrays.asList(projects);
  }

  public AnalysisResultDto getLinkageEvaluation(String projectId,
    Collection<RecordDto> plainTextRecords) {
    return getLinkageEvaluation(projectId, plainTextRecords, new HashMap<>());
  }

  public AnalysisResultDto getLinkageEvaluation(String projectId,
    Collection<RecordDto> plainTextRecords, Map<String, String> params) {
    return Unirest.post(url + "/analysis/eval/")
      .body(MatchResultAnalysisRequestDto.builder()
        .analysisRequest(AnalysisRequestDto.builder().projectId(projectId).parameters(params).build())
        .matchResult(MatchResultDto.builder()
          .records(plainTextRecords)
          .build()
        )
        .build()
      )
      .asObject(AnalysisResultDto.class)
      .getBody();
  }

  public MatchingDto createMatching(MatchingCreationRequestDto requestDto) {
    String endPoint = url + "/config/create";
    return Unirest.post(endPoint)
            .body(requestDto)
            .asObject(MatchingDto.class)
            .getBody();
  }
}

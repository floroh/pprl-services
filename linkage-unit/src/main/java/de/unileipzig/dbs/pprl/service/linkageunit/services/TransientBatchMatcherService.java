package de.unileipzig.dbs.pprl.service.linkageunit.services;

import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.matcher.matcher.BatchMatcher;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import de.unileipzig.dbs.pprl.service.common.data.converter.AbstractRecordConverter;
import de.unileipzig.dbs.pprl.service.common.data.converter.RecordConverter;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordIdDto;
import de.unileipzig.dbs.pprl.service.linkageunit.config.MatcherConfig;
import de.unileipzig.dbs.pprl.service.linkageunit.data.converter.RecordPairDtoConverter;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.BatchMatchRequestDto;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.ClusteringRequestDto;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.MatchResultDto;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.MatcherIdDto;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.RecordPairDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TransientBatchMatcherService extends AbstractMatcherService {

  private final Map<String, BatchMatcher> matcher;

  public TransientBatchMatcherService(MatcherConfig config,
    MatcherProviderService matcherProviderService) {
    super(new RecordConverter(), config, matcherProviderService);
    matcher = new HashMap<>();
  }

  private BatchMatcher getMatcher(String matcherName) {
    if (matcherName == null) {
      throw new RuntimeException("Invalid matcher name: null");
    }
    if (!matcher.containsKey(matcherName)) {
      MatcherIdDto matcherId = MatcherIdDto.builder().method(matcherName).build();
      matcher.put(
        matcherName,
        (BatchMatcher) matcherProviderService.getMatcher(matcherId)
      );
    }
    return matcher.get(matcherName);
  }

  public void removeMatcher(MatcherIdDto matcherIdDto) {
    log.info("Removing matcher for id: " + matcherIdDto);
    int preCount = matcher.size();
    matcher.entrySet().removeIf(e -> e.getKey().equals(matcherIdDto.getMethod()));
    int diff = preCount - matcher.size();
    log.info("Removed " + diff + " matcher");
  }

  public MatchResultDto match(BatchMatchRequestDto matchRequest) {
    //TODO Check if encoding method is supported by the requested matcher
    BatchMatcher curMatcher = getMatcher(matchRequest.getMethod());

    List<Record> in = matchRequest.getRecords().stream()
      .map(this::toRecord)
      .collect(Collectors.toList());
    Collection<RecordPair> classifiedRecordPairs = curMatcher.matchRecords(in);
    Collection<Record> records = curMatcher.assignGlobalIds(classifiedRecordPairs);
//    log.debug("Found " + records.size() + " unique records");

    List<RecordPairDto> recordPairDtos = classifiedRecordPairs.stream()
        .map(RecordPairDtoConverter::convertRecordPairToDto)
        .collect(Collectors.toList());
    List<RecordIdDto> recordIdDtos = convertRecordsToIdDtos(records);

    return MatchResultDto.builder()
      .recordIds(recordIdDtos)
      .recordPairs(recordPairDtos)
      .build();
  }

  public MatchResultDto cluster(ClusteringRequestDto clusteringRequestDto) {
    BatchMatcher curMatcher = getMatcher(clusteringRequestDto.getMethod());
    Collection<RecordPair> classifiedRecordPairs = clusteringRequestDto.getRecordPairs().stream()
        .map(RecordPairDtoConverter::convertDtoToRecordPair)
        .collect(Collectors.toList());
    Collection<Record> records = curMatcher.assignGlobalIds(classifiedRecordPairs);
    log.debug("Found " + records.size() + " unique records");

    List<RecordIdDto> recordIdDtos = convertRecordsToIdDtos(records);

    return MatchResultDto.builder()
      .recordIds(recordIdDtos)
      .build();
  }

  List<RecordIdDto> convertRecordsToIdDtos(Collection<Record> records) {
    return records.stream()
      .map(Record::getId)
      .map(AbstractRecordConverter::fromRecordId)
      .collect(Collectors.toList());
  }
}

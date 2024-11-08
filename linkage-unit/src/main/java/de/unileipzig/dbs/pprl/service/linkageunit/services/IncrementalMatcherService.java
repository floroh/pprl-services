package de.unileipzig.dbs.pprl.service.linkageunit.services;

import de.unileipzig.dbs.pprl.service.common.data.converter.AbstractRecordConverter;
import de.unileipzig.dbs.pprl.service.common.data.converter.MongoRecordConverter;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordIdDto;
import de.unileipzig.dbs.pprl.service.common.services.DatasetMongoService;
import de.unileipzig.dbs.pprl.service.common.services.MetricsService;
import de.unileipzig.dbs.pprl.service.linkageunit.config.MatcherConfig;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.MatcherIdDto;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.SearchResultDto;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.SearchResultEntryDto;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordId;
import de.unileipzig.dbs.pprl.core.matcher.matcher.IncrementalMatcher;
import de.unileipzig.dbs.pprl.core.matcher.model.api.SearchResult;
import de.unileipzig.dbs.pprl.core.matcher.model.api.SearchResultEntry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
@Slf4j
public class IncrementalMatcherService extends AbstractMatcherService {

  private IncrementalMatcher matcher;

  private DatasetMongoService datasetService;

  public IncrementalMatcherService(MatcherConfig config,
    MatcherProviderService matcherProviderService,
    DatasetMongoService datasetService) {
    super(new MongoRecordConverter(), config, matcherProviderService);
    this.datasetService = datasetService;
  }

  @PostConstruct
  private void init() {
    if (config.getIncrementalName() != null) {
      log.info("Using incremental matcher: {}", config.getIncrementalName());
      MatcherIdDto matcherId = MatcherIdDto.builder().method(config.getIncrementalName()).build();
      matcher = (IncrementalMatcher) matcherProviderService.getMatcher(matcherId);
    } else {
      log.warn("No incremental matcher configured! Incremental matcher will not work.");
    }
  }

  private void initMatcher(int datasetId) {
    matcher.setDataSet(datasetService.getBlockedDataSet(datasetId));
  }

  public RecordIdDto insert(RecordDto dtoIn) {
    initMatcher(dtoIn.getDatasetId());
    //TODO Validate record and reject if crucial attributes are missing / unplausible
    //TODO Check if encoding method is supported by the matcher
    Record in = toRecord(dtoIn);
    RecordId id = MetricsService.timer("insert.matcher.time").record(() -> matcher.insert(in));
    MetricsService.counter("insert.counter").increment();
    return AbstractRecordConverter.fromRecordId(id);
  }

  public SearchResultDto search(RecordDto dtoIn) {
    initMatcher(dtoIn.getDatasetId());
    //TODO Check if encoding method is supported by the matcher
    Record in = toRecord(dtoIn);
    SearchResult searchResult = matcher.search(in);
    MetricsService.summary("search.result.size").record(searchResult.getEntries().size());
    log.debug("Found " + searchResult.getEntries().size() + " matches");
    return createSearchResultDto(dtoIn.getId(), searchResult);
  }

  private SearchResultDto createSearchResultDto(RecordIdDto queryid, SearchResult searchResult) {
    SearchResultDto.SearchResultDtoBuilder builder = SearchResultDto.builder();
    builder.queryId(queryid);
    for (SearchResultEntry entry : searchResult.getEntries()) {
      RecordId foundId = entry.getRecord().getId();
      builder.match(
        new SearchResultEntryDto(AbstractRecordConverter.fromRecordId(foundId), entry.getSimilarity()));
    }
    return builder.build();
  }
}

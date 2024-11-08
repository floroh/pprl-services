package de.unileipzig.dbs.pprl.service.linkageunit.services;

import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import de.unileipzig.dbs.pprl.core.matcher.classification.Classifier;
import de.unileipzig.dbs.pprl.core.matcher.classification.TrainableClassifier;
import de.unileipzig.dbs.pprl.core.matcher.linking.DefaultLinker;
import de.unileipzig.dbs.pprl.core.matcher.matcher.DatasetBasedBatchMatcher;
import de.unileipzig.dbs.pprl.core.matcher.matcher.Matcher;
import de.unileipzig.dbs.pprl.service.common.data.converter.AbstractRecordConverter;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordDto;
import de.unileipzig.dbs.pprl.service.linkageunit.config.MatcherConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;

@Slf4j
public abstract class AbstractMatcherService {

  protected final AbstractRecordConverter<RecordDto> dtoInConverter;

  protected final MatcherConfig config;

  protected final MatcherProviderService matcherProviderService;

  public AbstractMatcherService(
    AbstractRecordConverter<RecordDto> dtoInConverter,
    MatcherConfig config,
    MatcherProviderService matcherProviderService) {
    this.dtoInConverter = dtoInConverter;
    this.config = config;
    this.matcherProviderService = matcherProviderService;
  }

  public Matcher trainMatcher(Matcher matcher, boolean update, Collection<RecordPair> labeledPairs) {
    if (matcher instanceof DatasetBasedBatchMatcher) {
      DatasetBasedBatchMatcher batchMatcher = (DatasetBasedBatchMatcher) matcher;
      DefaultLinker linker = (DefaultLinker) ((DatasetBasedBatchMatcher) matcher).getLinker();
      Classifier classifier = linker.getClassifier();
      if (classifier instanceof TrainableClassifier) {
        TrainableClassifier updatableClassifier = (TrainableClassifier) classifier;
        if (update) {
          updatableClassifier.update(labeledPairs);
        } else {
          updatableClassifier.fit(labeledPairs);
        }
        return batchMatcher;
      }
    }
    log.warn("Matcher does not support training: " + matcher);
    return matcher;
  }

  protected Record toRecord(RecordDto dto) {
    return dtoInConverter.toRecord(dto);
  }

}

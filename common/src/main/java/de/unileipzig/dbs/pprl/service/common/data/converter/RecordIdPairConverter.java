package de.unileipzig.dbs.pprl.service.common.data.converter;

import de.unileipzig.dbs.pprl.core.common.model.api.RecordIdPair;
import de.unileipzig.dbs.pprl.core.matcher.classification.Classifier;
import de.unileipzig.dbs.pprl.service.common.data.dto.GroundTruthDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordIdPairDto;
import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoGroundTruth;
import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoRecordIdPair;

import java.util.stream.Collectors;

public class RecordIdPairConverter {

  public static GroundTruthDto toDto(MongoGroundTruth groundTruth) {
    return GroundTruthDto.builder()
      .datasetId(groundTruth.getDatasetId())
      .recordIdPairs(groundTruth.getRecordIdPairs().stream()
        .map(RecordIdPairConverter::toDto)
        .collect(Collectors.toList()))
      .build();
  }

  public static MongoGroundTruth fromDto(GroundTruthDto groundTruthDto) {
    return new MongoGroundTruth(
      groundTruthDto.getDatasetId(),
      groundTruthDto.getRecordIdPairs().stream()
        .map(RecordIdPairConverter::fromDto)
        .collect(Collectors.toList())
    );
  }

  public static MongoRecordIdPair fromDto(RecordIdPairDto dto) {
    MongoRecordIdPair mongoRecordIdPair = new MongoRecordIdPair(
      AbstractRecordConverter.toRecordId(dto.getLeftRecordId()),
      AbstractRecordConverter.toRecordId(dto.getRightRecordId())
    );
    mongoRecordIdPair.setLabel(dto.getLabel() == null ? Classifier.Label.TRUE_MATCH : dto.getLabel());
    return mongoRecordIdPair;
  }

  public static RecordIdPairDto toDto(MongoRecordIdPair recordIdPair) {
    RecordIdPairDto dto = toDto((RecordIdPair)recordIdPair);
    dto.setLabel(recordIdPair.getLabel());
    return dto;
  }

  public static RecordIdPairDto toDto(RecordIdPair recordIdPair) {
    return RecordIdPairDto.builder()
      .leftRecordId(AbstractRecordConverter.fromRecordId(recordIdPair.getLeftRecordId()))
      .rightRecordId(AbstractRecordConverter.fromRecordId(recordIdPair.getRightRecordId()))
      .build();
  }
}

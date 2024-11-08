package de.unileipzig.dbs.pprl.service.common.data.converter;

import de.unileipzig.dbs.pprl.service.common.data.dto.DatasetDto;
import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoDataset;

public class DatasetConverter {

  public static DatasetDto toDto(MongoDataset dataset) {
    return DatasetDto.builder()
      .datasetId(dataset.getDatasetId())
      .datasetName(dataset.getDatasetName())
      .plaintextDatasetId(dataset.getPlaintextDatasetId())
      .build();
  }

  public static MongoDataset fromDto(DatasetDto dto) {
    return MongoDataset.builder()
      .datasetId(dto.getDatasetId())
      .datasetName(dto.getDatasetName())
      .plaintextDatasetId(dto.getPlaintextDatasetId())
      .build();
  }
}

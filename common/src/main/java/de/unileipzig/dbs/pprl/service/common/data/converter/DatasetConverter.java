package de.unileipzig.dbs.pprl.service.common.data.converter;

import de.unileipzig.dbs.pprl.service.common.data.dto.DatasetDto;
import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoDataset;

public class DatasetConverter {

  public static DatasetDto toDto(MongoDataset dataset) {
    return DatasetDto.builder()
            .datasetId(dataset.getDatasetId())
            .datasetName(dataset.getDatasetName())
            .plaintextDatasetId(dataset.getPlaintextDatasetId())
            .encodingIdDto(dataset.getEncodingIdDto())
            .properties(dataset.getProperties())
            .build();
  }

  public static MongoDataset fromDto(DatasetDto dto) {
    return MongoDataset.builder()
            .datasetId(dto.getDatasetId())
            .datasetName(dto.getDatasetName())
            .plaintextDatasetId(dto.getPlaintextDatasetId())
            .encodingIdDto(dto.getEncodingIdDto())
            .properties(dto.getProperties())
            .build();
  }
}

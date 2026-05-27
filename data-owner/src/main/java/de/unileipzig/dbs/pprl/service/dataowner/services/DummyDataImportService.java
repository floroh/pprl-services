package de.unileipzig.dbs.pprl.service.dataowner.services;

import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import de.unileipzig.dbs.pprl.service.common.csv.CsvImporter;
import de.unileipzig.dbs.pprl.service.common.csv.PersonRecord;
import de.unileipzig.dbs.pprl.service.common.data.dto.DatasetDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordDto;
import de.unileipzig.dbs.pprl.service.common.services.DatasetDtoService;
import de.unileipzig.dbs.pprl.service.dataowner.config.CsvDatasetConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DummyDataImportService {

  private final DatasetDtoService datasetDtoService;

  private final CsvDatasetConfig config;

  public DummyDataImportService(DatasetDtoService datasetDtoService,
    CsvDatasetConfig config) {
    this.datasetDtoService = datasetDtoService;
    this.config = config;
  }

  @PostConstruct
  private void autoImportCsv() throws IOException {
    List<CsvDatasetConfig.SingleCsvDatasetConfig> datasetConfigs = config.getDatasetConfigs();
    if (datasetConfigs == null || datasetConfigs.isEmpty()) {
      return;
    }
    for (CsvDatasetConfig.SingleCsvDatasetConfig datasetConfig : datasetConfigs) {
      importCsv(datasetConfig);
    }
  }

  public Optional<Long> importCsv(CsvDatasetConfig.SingleCsvDatasetConfig datasetConfig) throws IOException {
    log.info("Importing dataset with config: {}", datasetConfig);
    List<Long> existingDatasetIds = datasetDtoService.getDatasetIds();
    String location = datasetConfig.getLocation();
    long datasetId = datasetConfig.getDatasetId();
    if (datasetId == 0) {
      datasetId = -10;
      while (existingDatasetIds.contains(datasetId)) {
          datasetId--;
      }
      log.warn("Dataset Id must not be empty or 0. Generated a new one: {}", datasetId);
    }
    log.info("Importing CSV dataset from {} with id {}.", location, datasetId);
    if (location != null) {
      CsvImporter importer = new CsvImporter(location);
      importer.setSchema(
        CsvSchema.builder()
          .setUseHeader(true)
          .build()
      );
      List<PersonRecord> csvRecords = importer.getRecords();
      if (datasetConfig.getSource() != null) {
        csvRecords = csvRecords.stream()
          .filter(r -> r.getId().getSource().equals(datasetConfig.getSource()))
          .toList();
      }
      long finalDatasetId = datasetId;
      List<RecordDto> records = csvRecords.stream()
        .map(PersonRecord::toRecordDto)
        .peek(dto -> dto.setDatasetId(finalDatasetId))
        .collect(Collectors.toList());
      if (existingDatasetIds.contains(datasetId)) {
        if (config.isReplaceExisting()) {
          log.warn("Dataset with id {} already exists. It is replaced.", datasetId);
          datasetDtoService.deleteAllRecords(datasetId);
        } else {
          log.warn("Dataset with id {} already exists. Skipping the import.", datasetId);
          return Optional.empty();
        }
      }
      datasetDtoService.addDataset(DatasetDto.builder()
              .datasetId(datasetId)
              .datasetName("Csv dataset from " + location)
              .build()
      );
      datasetDtoService.insertAll(records);
    }
    return Optional.of(datasetId);
  }

}

package de.unileipzig.dbs.pprl.service.protocol.service;

import de.unileipzig.dbs.pprl.core.common.monitoring.Tag;
import de.unileipzig.dbs.pprl.service.common.data.dto.DatasetDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordDto;
import de.unileipzig.dbs.pprl.service.common.services.DatasetIdService;
import de.unileipzig.dbs.pprl.service.generator.data.dto.TaggedDatasetDto;
import de.unileipzig.dbs.pprl.service.protocol.api.EncoderApi;
import de.unileipzig.dbs.pprl.service.protocol.api.GeneratorApi;
import de.unileipzig.dbs.pprl.service.protocol.config.ServicesConfig;
import de.unileipzig.dbs.pprl.service.protocol.model.dto.DatasetCsvDto;
import de.unileipzig.dbs.pprl.service.protocol.model.dto.DatasetGeneratorDto;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for managing Data Owner PPRL services
 */
@Service
@Slf4j
public class DataOwnerService {

  private final ServicesConfig config;

  @Getter
  private final DatasetIdService datasetIdService;

  @Getter
  private final EncoderApi encoderApi = new EncoderApi();

  @Getter
  private final GeneratorApi generatorApi = new GeneratorApi();

  public DataOwnerService(ServicesConfig config, DatasetIdService datasetIdService) {
    this.config = config;
    this.datasetIdService = datasetIdService;
  }

  @PostConstruct
  private void initApi() {
    log.info("Initialising DO Service with url: {}", config.getDataOwnerEndpoint());
    encoderApi.setUrl(config.getDataOwnerEndpoint());
    generatorApi.setUrl(config.getDataGeneratorEndpoint());
  }

  public long insertDatasetFromCsv(DatasetCsvDto datasetCsvDto) {
    DatasetDto datasetDto = datasetCsvDto.getDatasetDto();
    if (datasetDto == null) {
      datasetDto = new DatasetDto();
    }
    if (datasetDto.getDatasetId() == null || datasetDto.getDatasetId() == 0) {
      datasetDto.setDatasetId(datasetIdService.generateDatasetId());
      datasetDto.getProperties().put("auto-generated-id", "true");
    }
    if (datasetDto.getDatasetName() == null) {
      datasetDto.setDatasetName(String.format("DatasetCsv from %s", datasetCsvDto.getPath()));
    }
    datasetCsvDto.setDatasetDto(datasetDto);
    this.encoderApi.insertDatasetFromCsv(datasetCsvDto);
    return datasetDto.getDatasetId();
  }

  public long addGeneratedDataset(DatasetGeneratorDto datasetGeneratorDto) {
    DatasetDto datasetDto = DatasetDto.builder()
            .datasetId(datasetGeneratorDto.getDatasetId())
            .build();
    if (datasetGeneratorDto.getDatasetId() == null || datasetGeneratorDto.getDatasetId() == 0) {
      long datasetId = datasetIdService.generateDatasetId();
      datasetGeneratorDto.setDatasetId(datasetId);
      datasetDto.setDatasetId(datasetId);
      datasetDto.getProperties().put("auto-generated-id", "true");
    }
    log.info("Adding generated dataset with configuration: {}", datasetGeneratorDto);
    String generatedDatasetName = datasetGeneratorDto.getDatasetName();
    TaggedDatasetDto taggedDatasetDto = null;
    if (datasetGeneratorDto.getGermanyGeneratorConfig() != null) {
      taggedDatasetDto = generatorApi.generateGermanRecords(datasetGeneratorDto.getGermanyGeneratorConfig());
      if (generatedDatasetName == null) {
        generatedDatasetName = datasetGeneratorDto.getGermanyGeneratorConfig().getName();
      }
      datasetDto.setDatasetName(generatedDatasetName);

    } else if (datasetGeneratorDto.getUsvrSelectionConfig() != null) {
      taggedDatasetDto = generatorApi.selectUsvrRecords(datasetGeneratorDto.getUsvrSelectionConfig());
      if (generatedDatasetName == null) {
        generatedDatasetName = datasetGeneratorDto.getUsvrSelectionConfig().getName(true, true, true);
      }
      datasetDto.setDatasetName(generatedDatasetName);
    }
    if (taggedDatasetDto == null || taggedDatasetDto.getRecords() == null) {
      log.warn("No records where generated. Abort.");
      return 0;
    }
    List<RecordDto> recordDtos = taggedDatasetDto.getRecords().stream()
            .peek(r -> r.setDatasetId(datasetGeneratorDto.getDatasetId()))
            .toList();
    log.info("Adding {} records to dataset: {}", recordDtos.size(), datasetGeneratorDto);
    encoderApi.deleteDataset(datasetGeneratorDto.getDatasetId());
    encoderApi.addDatasetDescription(datasetDto);
    encoderApi.batchInsert(recordDtos);
    if (taggedDatasetDto.getTags() != null) {
      encoderApi.addTags(datasetGeneratorDto.getDatasetId(), taggedDatasetDto.getTags());
    }
    return datasetGeneratorDto.getDatasetId();
  }
}

package de.unileipzig.dbs.pprl.service.dataowner.services;

import de.unileipzig.dbs.pprl.service.common.utils.LocalConfigHandlerUtils;
import de.unileipzig.dbs.pprl.core.encoder.RecordEncoderSerialization;
import de.unileipzig.dbs.pprl.core.encoder.record.RecordEncoder;
import de.unileipzig.dbs.pprl.service.common.data.dto.EncodingDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.EncodingIdDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordRequirementsDto;
import de.unileipzig.dbs.pprl.service.dataowner.config.LocalEncodingsConfig;
import de.unileipzig.dbs.pprl.service.dataowner.data.mongo.MongoEncodingConfig;
import de.unileipzig.dbs.pprl.service.dataowner.persistence.repositories.EncodingConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EncoderProviderService {
  public static final String FILENAME_ENCODING = "encoder.json";
  public static final String FILENAME_META = "meta.json";
  public static final String FILENAME_VALIDATION = "validation.json";

  private final LocalEncodingsConfig localEncodingsConfig;

  private List<EncodingDto> encodings;

  private EncodingConfigRepository configRepository;

  public EncoderProviderService(
          LocalEncodingsConfig localEncodingsConfig, EncodingConfigRepository configRepository) {
    this.localEncodingsConfig = localEncodingsConfig;
    this.configRepository = configRepository;
  }

  @PostConstruct
  public void readLocallyAvailableEncodings() {
    List<Path> paths = LocalConfigHandlerUtils.scanForLocallyAvailableConfigs();
    if (localEncodingsConfig.getPaths() != null) {
      localEncodingsConfig.getPaths().stream()
        .flatMap(s -> LocalConfigHandlerUtils.scanForConfigs(s).stream())
        .forEach(paths::add);
    }

    encodings = new ArrayList<>();
    for (Path path : paths) {
      EncodingIdDto id;
      String config;
      try {
        id = LocalConfigHandlerUtils.parse(path.resolve(FILENAME_META), EncodingIdDto.class);
        config = LocalConfigHandlerUtils
          .readFileContent(path.resolve(FILENAME_ENCODING)).replaceAll("\\s+", "");

      } catch (IOException e) {
        log.error("Failed to read config from: {}", path);
        continue;
      }

      EncodingDto.EncodingDtoBuilder encodingBuilder = EncodingDto.builder()
        .id(id)
        .config(config);

      try {
        RecordRequirementsDto validation =
          LocalConfigHandlerUtils.parse(path.resolve(FILENAME_VALIDATION), RecordRequirementsDto.class);
        encodingBuilder.validation(validation);
      } catch (
        InvalidPathException | IOException e) {
        log.debug("No validation description found for encoding: {}", id);
      }

      EncodingDto encoding = encodingBuilder.build();
      log.info("Found encoding: {}", encoding.getId());
      encodings.add(encoding);
      Optional<MongoEncodingConfig> existingConfig = getEncodingConfigById(encoding.getId());
      if (existingConfig.isPresent()) {
        log.info("Encoding already exists, replacing it.");
        configRepository.delete(existingConfig.get());
      }
      configRepository.save(MongoEncodingConfig.builder()
              .encodingDto(encoding)
              .build()
      );
    }
  }

  public Collection<EncodingDto> getAvailableEncodings() {
    log.debug("Fetching available encodings");
    return configRepository.findAll().stream()
            .map(MongoEncodingConfig::getEncodingDto)
            .collect(Collectors.toList());
  }

  public Optional<EncodingDto> getById(EncodingIdDto idDto) {
    return getEncodingConfigById(idDto).stream()
            .map(MongoEncodingConfig::getEncodingDto)
            .findFirst();
  }

  private Optional<MongoEncodingConfig> getEncodingConfigById(EncodingIdDto idDto) {
    Collection<MongoEncodingConfig> configs =
            configRepository.findMongoEncodingConfigsByEncodingDto_Id_Method(idDto.getMethod());
    if (configs.isEmpty()) {
      return Optional.empty();
    }
    if (idDto.getProject() != null) {
      Optional<MongoEncodingConfig> projectSpecificMatching = configs.stream()
              .filter(config -> idDto.getProject().equals(config.getEncodingDto().getId().getProject()))
              .findFirst();
      if (projectSpecificMatching.isPresent()) {
        return projectSpecificMatching;
      }
    }
    return configs.stream()
            .filter(config -> config.getEncodingDto().getId().getProject() == null)
            .findFirst();
  }

  public void addEncoding(EncodingDto encodingDto) {
    log.info("Adding encoding config with id: {}", encodingDto.getId());
//    log.debug("Config content: {}", encodingDto.getConfig());
    if (getEncodingConfigById(encodingDto.getId()).isPresent()) {
      throw new RuntimeException("Trying to add an already existing matcher. Use Update method instead.");
    }
    configRepository.save(MongoEncodingConfig.builder()
            .encodingDto(encodingDto)
            .build()
    );
  }

  public void updateEncoding(EncodingDto encodingDto) {
    log.info("Updating encoding config with id: {}", encodingDto.getId());
    log.debug("Config content: {}", encodingDto.getConfig());
    Optional<MongoEncodingConfig> config = getEncodingConfigById(encodingDto.getId());
    if (config.isPresent()) {
      config.get().getEncodingDto().setConfig(encodingDto.getConfig());
      configRepository.save(config.get());
    } else {
      throw new RuntimeException("Trying to update missing matching with id: " + encodingDto.getId());
    }
  }

  public void removeEncoding(EncodingIdDto encodingIdDto) {
    log.info("Removing encodings for id: {}", encodingIdDto);
    int preCount = configRepository.findAll().size();
    if (encodingIdDto.getProject() != null) {
      configRepository.deleteAllByEncodingDto_Id_MethodAndEncodingDto_Id_Project(encodingIdDto.getMethod(), encodingIdDto.getProject());
    } else {
      configRepository.deleteAllByEncodingDto_Id_Method(encodingIdDto.getMethod());
    }
    int diff = preCount - configRepository.findAll().size();
    log.info("Removed {} encodings", diff);
  }

  public RecordEncoder getEncoder(EncodingIdDto idDto) {
    EncodingDto encoding = getById(idDto)
      .orElseThrow(() -> new RuntimeException("Unknown encoder with id: " + idDto));

    log.info("Parsing RecordEncoder with name {}", idDto);
    try {
      return RecordEncoderSerialization.deserializeJson(encoding.getConfig());
    } catch (IOException e) {
      throw new RuntimeException("Failed to parse RecordEncoder: " + encoding.getId() + " with " +
        e.fillInStackTrace());
    }
  }

}

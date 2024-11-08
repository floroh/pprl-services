package de.unileipzig.dbs.pprl.service.dataowner.services;

import de.unileipzig.dbs.pprl.service.common.utils.LocalConfigHandlerUtils;
import de.unileipzig.dbs.pprl.core.encoder.RecordEncoderSerialization;
import de.unileipzig.dbs.pprl.core.encoder.record.RecordEncoder;
import de.unileipzig.dbs.pprl.service.common.data.dto.EncodingDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.EncodingIdDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordRequirementsDto;
import de.unileipzig.dbs.pprl.service.dataowner.config.LocalEncodingsConfig;
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

@Service
@Slf4j
public class EncoderProviderService {
  public static final String FILENAME_ENCODING = "encoder.json";
  public static final String FILENAME_META = "meta.json";
  public static final String FILENAME_VALIDATION = "validation.json";

  private final LocalEncodingsConfig localEncodingsConfig;

  private List<EncodingDto> encodings;

  public EncoderProviderService(LocalEncodingsConfig localEncodingsConfig) {
    this.localEncodingsConfig = localEncodingsConfig;
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
    }
  }

  public Collection<EncodingDto> getAvailableEncodings() {
    log.debug("Fetching available encodings");
    return encodings;
  }

  public Optional<EncodingDto> getById(EncodingIdDto idDto) {
    return encodings.stream()
      .filter(encoding -> encoding.getId().getMethod().equals(idDto.getMethod()))
      .findFirst();
  }

  public void addEncoding(EncodingDto encodingDto) {
    log.info("Adding encoding config with id: {}", encodingDto.getId());
    log.debug("Config content: {}", encodingDto.getConfig());
    if (getById(encodingDto.getId()).isPresent()) {
      throw new RuntimeException("Trying to add an already existing encoding. Use Update method instead.");
    }
    encodings.add(encodingDto);
  }

  public void updateEncoding(EncodingDto encodingDto) {
    log.info("Updating encoding config with id: {}", encodingDto.getId());
    log.debug("Config content: {}", encodingDto.getConfig());
    Optional<EncodingDto> existingEncoding = getById(encodingDto.getId());
    if (existingEncoding.isPresent()) {
      existingEncoding.get().setConfig(encodingDto.getConfig());
    } else {
      throw new RuntimeException("Trying to update missing encoding with id: " + encodingDto.getId());
    }
  }

  public void removeEncoding(EncodingIdDto encodingIdDto) {
    log.info("Removing encodings for id: {}", encodingIdDto);
    int preCount = encodings.size();
    encodings.removeIf(knownEncoding -> knownEncoding.getId().isSubtypeOf(encodingIdDto));
    int diff = preCount - encodings.size();
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

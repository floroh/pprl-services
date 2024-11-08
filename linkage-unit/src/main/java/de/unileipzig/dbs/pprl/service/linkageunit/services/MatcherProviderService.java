package de.unileipzig.dbs.pprl.service.linkageunit.services;

import de.unileipzig.dbs.pprl.core.matcher.MatcherSerialization;
import de.unileipzig.dbs.pprl.core.matcher.matcher.Matcher;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordRequirementsDto;
import de.unileipzig.dbs.pprl.service.common.utils.LocalConfigHandlerUtils;
import de.unileipzig.dbs.pprl.service.linkageunit.config.LocalMatcherConfig;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.MatcherIdDto;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.MatchingDto;
import de.unileipzig.dbs.pprl.service.linkageunit.data.mongo.LinkageConfig;
import de.unileipzig.dbs.pprl.service.linkageunit.persistence.repositories.LinkageUnitConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MatcherProviderService {
  public static final String FILENAME_MATCHER = "matcher.json";
  public static final String FILENAME_META = "meta.json";
  public static final String FILENAME_VALIDATION = "validation.json";

  private final LocalMatcherConfig localMatcherConfig;

  private final LinkageUnitConfigRepository configRepository;

  public MatcherProviderService(
    LocalMatcherConfig localMatcherConfig, LinkageUnitConfigRepository configRepository) {
    this.localMatcherConfig = localMatcherConfig;
    this.configRepository = configRepository;
  }

  @PostConstruct
  public void readLocallyAvailableEncodings() {
    List<Path> paths = LocalConfigHandlerUtils.scanForLocallyAvailableConfigs();
    if (localMatcherConfig.getPaths() != null) {
      localMatcherConfig.getPaths().stream()
        .flatMap(s -> LocalConfigHandlerUtils.scanForConfigs(s).stream())
        .forEach(paths::add);
    }

    for (Path path : paths) {
      MatcherIdDto id;
      String config;
      try {
        id = LocalConfigHandlerUtils.parse(path.resolve(FILENAME_META), MatcherIdDto.class);
        config = LocalConfigHandlerUtils
//          .readFileContent(path.resolve(FILENAME_MATCHER)).replaceAll("\\s+", "");
          .readFileContent(path.resolve(FILENAME_MATCHER));
      } catch (IOException e) {
        log.error("Failed to read config from: " + path);
        continue;
      }

      MatchingDto.MatchingDtoBuilder builder = MatchingDto.builder()
        .id(id)
        .config(config);

      try {
        RecordRequirementsDto validation =
          LocalConfigHandlerUtils.parse(path.resolve(FILENAME_VALIDATION), RecordRequirementsDto.class);
        builder.validation(validation);
      } catch (
        InvalidPathException | IOException e) {
        log.debug("No validation description found for matcher: " + id);
      }

      MatchingDto matching = builder.build();
      log.info("Found matcher: " + matching.getId());
      Collection<LinkageConfig> existingConfigs =
        configRepository.findByPartialAndMatchingDto_Id_Method(false, matching.getId().getMethod());
      if (!existingConfigs.isEmpty()) {
        log.info("Matcher already exists, replacing it.");
        configRepository.deleteAll(existingConfigs);
      }
      configRepository.save(LinkageConfig.builder()
        .matchingDto(matching)
        .partial(false)
        .build()
      );
    }
  }

  public Collection<MatchingDto> getAvailableMatchers() {
    log.debug("Fetching available matchers");
    return configRepository.findAllByPartial(false).stream()
      .map(LinkageConfig::getMatchingDto)
      .collect(Collectors.toList());
  }

  public Optional<MatchingDto> getMatchingById(MatcherIdDto idDto) {
    return getLinkageConfigById(idDto).stream()
      .map(LinkageConfig::getMatchingDto)
      .findFirst();
  }

  public Optional<LinkageConfig> getLinkageConfigById(MatcherIdDto idDto) {
    Collection<LinkageConfig> configs =
      configRepository.findByPartialAndMatchingDto_Id_Method(false, idDto.getMethod());
    if (configs.isEmpty()) {
      return Optional.empty();
    }
    if (idDto.getProject() != null) {
      Optional<LinkageConfig> projectSpecificMatching = configs.stream()
//        .map(LinkageConfig::getMatchingDto)
        .filter(config -> idDto.getProject().equals(config.getMatchingDto().getId().getProject()))
        .findFirst();
      if (projectSpecificMatching.isPresent()) {
        return projectSpecificMatching;
      }
    }
    return configs.stream()
      .filter(config -> config.getMatchingDto().getId().getProject() == null)
      .findFirst();
  }

  public void add(MatchingDto matchingDto) {
    log.info("Adding matching config with id: " + matchingDto.getId());
//    log.debug("Config content: " + matchingDto.getConfig());
    if (getLinkageConfigById(matchingDto.getId()).isPresent()) {
      throw new RuntimeException("Trying to add an already existing matcher. Use Update method instead.");
    }
    configRepository.save(LinkageConfig.builder()
      .partial(false)
      .matchingDto(matchingDto)
      .build()
    );
  }

  public void update(MatchingDto matchingDto) {
    log.info("Updating matching config with id: " + matchingDto.getId());
//    log.debug("Config content: " + matchingDto.getConfig());
    Optional<LinkageConfig> config = getLinkageConfigById(matchingDto.getId());
    if (config.isPresent()) {
      config.get().getMatchingDto().setConfig(matchingDto.getConfig());
      configRepository.save(config.get());
    } else {
      throw new RuntimeException("Trying to update missing matching with id: " + matchingDto.getId());
    }
  }

  public void remove(MatcherIdDto matcherIdDto) {
    log.info("Removing matchings for id: " + matcherIdDto);
    int preCount = configRepository.findAllByPartial(false).size();
    configRepository.deleteAllByMatchingDto_Id_Method(matcherIdDto.getMethod());
    int diff = preCount - configRepository.findAllByPartial(false).size();
    log.info("Removed " + diff + " matchings");
  }

  public Matcher getMatcher(MatcherIdDto idDto) {
    MatchingDto matcher = getMatchingById(idDto)
      .orElseThrow(() -> new RuntimeException("Unknown matcher with id: " + idDto));

    log.debug("Parsing Matcher with name " + idDto);
    String jsonString = matcher.getConfig();
    try {
      if (jsonString.contains("IncrementalMatcher")) {
        return MatcherSerialization.deserializeJsonIncremental(jsonString);
      } else if (jsonString.contains("BatchMatcher")) {
        return MatcherSerialization.deserializeJsonBatch(jsonString);
      } else {
        throw new RuntimeException("Unknown type of matcher");
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to parse Matcher: " + idDto + " with error: " + e.getMessage());
    }
  }

}

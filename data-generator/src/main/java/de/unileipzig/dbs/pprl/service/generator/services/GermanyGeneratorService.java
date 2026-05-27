package de.unileipzig.dbs.pprl.service.generator.services;

import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.monitoring.Tag;
import de.unileipzig.dbs.pprl.core.common.monitoring.TagTable;
import de.unileipzig.dbs.pprl.service.common.data.converter.RecordConverter;
import de.unileipzig.dbs.pprl.service.common.data.dto.AttributeDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordDto;
import de.unileipzig.dbs.pprl.service.generator.data.dto.GermanyGeneratorConfig;
import de.unileipzig.dbs.pprl.service.generator.data.dto.TaggedDatasetDto;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.RecordDtoParser;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.builders.RandomGeneratorBuilder;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.households.Household;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.randomgenerator.RandomSingleton;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.records.Person;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.writers.RecordWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
@Slf4j
public class GermanyGeneratorService {

  public TaggedDatasetDto generate(GermanyGeneratorConfig config) {
    completeConfig(config);
    generateToCsv(config);

    List<String> csvFilePaths = new ArrayList<>();
    if (config.isIncludeHouseholdStructures()) {
      for (int p = 1; p <= 4; p++) {
        String path = Paths.get(
                config.getDestinationFolder(),
                config.getFileName() + "-phase" + p + ".csv"
        ).toString();
        csvFilePaths.add(path);
      }
    } else {
      csvFilePaths.add(Paths.get(
              config.getDestinationFolder(),
              config.getFileName() + ".csv"
      ).toString());
    }
    String sourceName = config.getSourceName();
    RecordDtoParser parser = (sourceName != null)
            ? new RecordDtoParser(sourceName)
            : new RecordDtoParser();
    TaggedDatasetDto outputDataset = new TaggedDatasetDto();
    List<RecordDto> recordDtos = parser.parseRecords(csvFilePaths, config.getAttributes());
    outputDataset.setRecords(recordDtos);

    RecordConverter converter = new RecordConverter();
    List<Record> records = recordDtos.stream()
            .peek(dto -> {
              if (!config.isIncludeHouseholdStructures()) {
                Map<String, AttributeDto> attributes = new HashMap<>(dto.getAttributes());
                attributes.remove("HOUSEHOLD-ID");
                attributes.remove("HOUSEHOLD-ROLE");
                attributes.remove("HOUSEHOLD-STRUCTURE");
                dto.setAttributes(attributes);
              }
            })
            .map(converter::toRecord)
            .toList();
    // Keep household attributes to allow for conditional corruption based on them
    TagTable tagTable = extractHouseholdTags(records, null, false);
    outputDataset.setTags(tagTable.getTagList());
    log.info("Extracted household tags: {}", outputDataset.getTags().size());
    return outputDataset;
  }

  private static void completeConfig(GermanyGeneratorConfig config) {
    config.setIncludeHeader(false);
    addTemporaryOutputDirectory(config);
    addTimestampFileName(config);
    addAttributesIfMissing(config);
  }

  private static void addAttributesIfMissing(GermanyGeneratorConfig config) {
    if (config.getAttributes() == null || config.getAttributes().isEmpty()) {
      config.setAttributes(List.of(
              "personId",
              "federalState",
              "gender",
              "age",
              "dateOfBirth",
              "surname",
              "forename",
              "zipCode",
              "location",
              "street",
              "placeOfBirth-FederalState",
              "placeOfBirth",
              "householdRole",
              "householdStructure",
              "householdId"
      ));
    }
  }

  private static void addTimestampFileName(GermanyGeneratorConfig config) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    String timestamp = LocalDateTime.now().format(formatter);
    config.setFileName(timestamp);
  }

  private static void addTemporaryOutputDirectory(GermanyGeneratorConfig config) {
    String tmpDir = "/tmp/tmp-generator-output";
    try {
      Path tmpDirPath = Files.createTempDirectory("tmp-gen-output");
      tmpDir = tmpDirPath.toAbsolutePath().toString();
    } catch (IOException e) {
      log.error("Could not get dynamic temporary output directory, using {} instead", tmpDir);
    }
    log.info("Using temporary directory: " + tmpDir);
    config.setDestinationFolder(tmpDir + "/");
  }


  public static TagTable extractHouseholdTags(Collection<Record> records, TagTable tagTable, boolean removeAttributes) {
    if (tagTable == null) {
      tagTable = new TagTable();
    }
    if (records.stream().anyMatch(r -> r.getAttribute("HOUSEHOLD-STRUCTURE").isPresent())) {
      log.info("Parsing household attributes to tags");
      for (Record preparedInputRecord : records) {
        Optional<Attribute> optionalHouseholdRole = preparedInputRecord.getAttribute("HOUSEHOLD-ROLE");
        for (String attributeName : List.of("HOUSEHOLD-STRUCTURE", "HOUSEHOLD-ID", "HOUSEHOLD-ROLE")) {
          Optional<Attribute> optionalAttribute = preparedInputRecord.getAttribute(attributeName);
          if (optionalAttribute.isPresent()) {
            Attribute attribute = optionalAttribute.get();
            String attrValue = attribute.getAsString();
            if (attributeName.equals("HOUSEHOLD-STRUCTURE")) {
              String householdType = null;
              if (attrValue.contains("-")) {
                householdType = "FAMILY";
              } else if (attrValue.equals("1") && optionalHouseholdRole.isPresent() &&
                      optionalHouseholdRole.get().getAsString().equals("FM0")) {
                householdType = "SINGLE";
              } else if (attrValue.equals("null")) {
                householdType = "UNKNOWN";
              } else {
                householdType = "FLATSHARE";
              }
              tagTable.addTag(preparedInputRecord.getId().getUniqueLikeId(),
                      null,
                      null,
                      "HOUSEHOLD-TYPE",
                      householdType,
                      null,
                      Tag.TYPE_STRUCTURE,
                      Tag.ORIGIN_DATA_GENERATOR
              );
            }
            if (attrValue != null && !attrValue.equalsIgnoreCase("null")) {
              tagTable.addTag(preparedInputRecord.getId().getUniqueLikeId(),
                      null,
                      null,
                      attributeName,
                      attrValue,
                      null,
                      Tag.TYPE_STRUCTURE,
                      Tag.ORIGIN_DATA_GENERATOR
              );
            }
            if (removeAttributes) {
              preparedInputRecord.removeAttribute(attributeName);
            }
          }
        }
      }
    }
    return tagTable;
  }

  public void generateToCsv(GermanyGeneratorConfig config) {
    long startTime = System.currentTimeMillis();

    RandomGeneratorBuilder.reset();
    RandomSingleton.getInstance().setSeed(config.getSeed());

    try {
      if (config.isIncludeHouseholdStructures()) {
        Household h = new Household(
                config.getAttributes(),
                config.getNumberOfRecords(),
                config.isIncludeHeader(),
                config.getDestinationFolder(),
                config.getFileName());
        h.generateHouseholds();
      } else {
        Person person = new Person(config.getAttributes());
        RecordWriter rw = new RecordWriter();
        rw.writeRecordToCsvFile(
                person,
                config.getNumberOfRecords(),
                config.isIncludeHeader(),
                config.getDestinationFolder(),
                config.getFileName());
      }

    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }
    long endTime = System.currentTimeMillis();
    log.info("Overall processing time: approx. {} minutes.", ((double) (endTime - startTime) / 1000) / 60);
  }

  public GermanyGeneratorConfig getGermanyGeneratorConfiguration(
          int numberOfRecords,
          boolean includeHouseholds
  ) {
    return GermanyGeneratorConfig.builder()
            .seed("abcd")
            .numberOfRecords(numberOfRecords)
            .includeHouseholdStructures(includeHouseholds)
            .includeHeader(false)
            .destinationFolder("output")
            .fileName("n" + numberOfRecords + (includeHouseholds ? ".hh" : ""))
            .attributes(List.of(
                    "Person_Id",
                    "FederalState",
                    "Gender",
                    "Age",
                    "DateOfBirth",
                    "Surname",
                    "Forename",
                    "ZipCode",
                    "Location",
                    "Street",
                    "PlaceOfBirth-FederalState",
                    "PlaceOfBirth",
                    "HouseholdRole",
                    "HouseholdStructure",
                    "Household_Id"
            ))
            .build();
  }
}

package de.unileipzig.dbs.pprl.service.common.controller;

import de.unileipzig.dbs.pprl.service.common.data.dto.AttributeDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.DatasetDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordIdDto;
import de.unileipzig.dbs.pprl.service.common.services.AnalysisService;
import de.unileipzig.dbs.pprl.service.common.services.DatasetDtoService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "record", produces = MediaType.APPLICATION_JSON_VALUE)
@Timed
@Tag(name = RecordController.TAG, description = "Manage records in the database")
@CrossOrigin
@Slf4j
public class RecordController {

  public static final String TAG = "Dataset management";

  private final DatasetDtoService datasetDtoService;
  private final AnalysisService analysisService;

  public RecordController(DatasetDtoService datasetDtoService, AnalysisService analysisService) {
    this.datasetDtoService = datasetDtoService;
    this.analysisService = analysisService;
  }

  @Operation(summary = "Add a record to the database", tags = TAG)
  @PostMapping()
  public RecordIdDto insert(@RequestBody RecordDto record) {
    return datasetDtoService.insert(record);
  }

  @Operation(summary = "Add multiple records to the database", tags = TAG)
  @PostMapping("/batch")
  public List<RecordIdDto> insertBatch(@RequestBody List<RecordDto> records) {
    return datasetDtoService.insertAll(records);
  }

  @Operation(summary = "Update an existing record in the database", tags = TAG)
  @PutMapping()
  public RecordIdDto update(@RequestBody RecordDto record) {
    return datasetDtoService.update(record);
  }

  @Operation(summary = "Get all records from the database", tags = TAG)
  @GetMapping("/{idDataset}/all")
  public List<RecordDto> getAll(@PathVariable int idDataset,
    @RequestParam(required = false, defaultValue = "-1") Integer limit) {
    List<RecordDto> allRecordsAsDto = datasetDtoService.getAllRecordsAsDto(idDataset);
    if (limit > 0) {
      allRecordsAsDto = allRecordsAsDto.subList(0, Math.min(allRecordsAsDto.size(), limit));
    }
    log.info("Responding with {} records from dataset {}", allRecordsAsDto.size(), idDataset);
    return allRecordsAsDto;
  }

  @Operation(summary = "Get the number of records in the database", tags = TAG)
  @GetMapping("/{idDataset}/count")
  public Long count(@PathVariable int idDataset) {
    Long numberOfRecords = datasetDtoService.getNumberOfRecords(idDataset);
    log.debug("Retrieved number of records ({}) from dataset {}", numberOfRecords, idDataset);
    return numberOfRecords;
  }

  @Operation(summary = "Retrieve a persisted record", tags = TAG)
  @GetMapping("/{uniqueId}")
  public RecordDto findByUniqueId(@PathVariable String uniqueId) {
    return datasetDtoService.getRecordAsDto(uniqueId);
  }

  @Operation(summary = "Retrieve multiple persisted records", tags = TAG)
  @PostMapping("/findByIds")
  public List<RecordDto> findByUniqueIds(@RequestBody List<String> uniqueIds) {
    return datasetDtoService.getRecordsAsDto(uniqueIds);
  }

  @Operation(summary = "Retrieve a persisted record by its record id (source + local)", tags = TAG)
  @PostMapping("/findByRecordId/{idDataset}")
  public RecordDto findByRecordId(@PathVariable String idDataset, @RequestBody RecordIdDto recordIdDto) {
    return datasetDtoService.getRecordAsDto(Integer.parseInt(idDataset), recordIdDto);
  }

  @Operation(summary = "Retrieve a persisted record by source", tags = TAG)
  @PostMapping("/findBySource/{idDataset}")
  public List<RecordDto> findByDatasetAndSource(@PathVariable String idDataset, @RequestBody String source) {
    return datasetDtoService.getRecordsBySourceAsDto(Integer.parseInt(idDataset), source);
  }

  @Operation(summary = "Clear the record database", tags = TAG)
  @DeleteMapping("/{idDataset}/all")
  public void deleteAll(@PathVariable int idDataset) {
    datasetDtoService.deleteAllRecords(idDataset);
    analysisService.deleteAnalysisResults(idDataset);
  }

  @Operation(summary = "Add a dataset description", tags = TAG)
  @PostMapping("/datasets")
  public DatasetDto addDatasetDescription(@RequestBody DatasetDto datasetDto) {
    return datasetDtoService.addDataset(datasetDto);
  }

  @Operation(summary = "Get available dataset ids", tags = TAG)
  @GetMapping("/datasets/ids")
  public List<Integer> getDatasetIds() {
    return datasetDtoService.getDatasetIds();
  }

  @Operation(summary = "Get dataset description", tags = TAG)
  @GetMapping("/datasets/{idDataset}")
  public Optional<DatasetDto> getDatasetDescription(@PathVariable int idDataset) {
    return datasetDtoService.getDataset(idDataset);
  }

  @Operation(summary = "Delete dataset", tags = TAG)
  @DeleteMapping("/datasets/{idDataset}")
  public void deleteDataset(@PathVariable int idDataset) {
    datasetDtoService.deleteDataset(idDataset);
    analysisService.deleteAnalysisResults(idDataset);
  }

  @Operation(summary = "Get available dataset descriptions, optionally filtered by plaintextDatasetId", tags =
    TAG)
  @GetMapping("/datasets")
  public List<DatasetDto> getDatasetDescriptions(@RequestParam(required = false) Integer plaintextDatasetId) {
    return datasetDtoService.getDatasets(Optional.ofNullable(plaintextDatasetId));
  }

  @Operation(summary = "Compare dataset", tags = TAG)
  @GetMapping("/compare/{idDataset0}/{idDataset1}")
  public Integer compareDatasets(@PathVariable int idDataset0, @PathVariable int idDataset1) {
    List<RecordDto> records0 = datasetDtoService.getAllRecordsAsDto(idDataset0);
    Set<String> fingerPrint0 = records0.stream()
      .map(RecordController::getAttributesFingerPrint)
      .collect(Collectors.toSet());
    List<RecordDto> records1 = datasetDtoService.getAllRecordsAsDto(idDataset1);
    long equalFingerPrintCount = 0;
    for (RecordDto record : records1) {
      if (fingerPrint0.contains(getAttributesFingerPrint(record))) {
        equalFingerPrintCount++;
      }
    }
    log.info("Found {} equal records between datasets {} ({} records) and {} ({} records)",
      equalFingerPrintCount, idDataset0, records0.size(), idDataset1, records1.size());
    return (int) equalFingerPrintCount;
  }

  private static String getAttributesFingerPrint(RecordDto dto) {
    return dto.getAttributes().values().stream().map(AttributeDto::getValue).collect(Collectors.joining("#"));
  }
}
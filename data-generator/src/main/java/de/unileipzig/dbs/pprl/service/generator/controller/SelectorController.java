package de.unileipzig.dbs.pprl.service.generator.controller;

import de.unileipzig.dbs.pprl.service.generator.data.dto.NcvrPanseImportRequest;
import de.unileipzig.dbs.pprl.service.generator.data.dto.TaggedDatasetDto;
import de.unileipzig.dbs.pprl.service.generator.data.dto.UsvrSelectionConfig;
import de.unileipzig.dbs.pprl.service.generator.data.dto.ClusterOrderRequest;
import de.unileipzig.dbs.pprl.service.generator.selection.model.common.RecordCluster;
import de.unileipzig.dbs.pprl.service.generator.services.ExampleProviderService;
import de.unileipzig.dbs.pprl.service.generator.services.UsSelectorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@RestController
@RequestMapping(value = "selector", produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@CrossOrigin
@Tag(name = "Data Selector", description = "API for selecting records from an existing dataset of record clusters")
public class SelectorController {

  private final UsSelectorService selectorService;
  private final ExampleProviderService exampleProviderService;

  public SelectorController(UsSelectorService selectorService, ExampleProviderService exampleProviderService) {
    this.selectorService = selectorService;
    this.exampleProviderService = exampleProviderService;
  }

  @PostMapping("/prepare/import-ncvr")
  @Operation(
          summary = "Import NCVR data",
          description = "Import record clusters from North Carolina Voter Registry as provied by Panse et al. at EDBT"
  )
  public void prepareImportPanseNcvr(@RequestBody NcvrPanseImportRequest request) {
    log.info("Received request: {}", request);
    this.selectorService.importPanseNcvr(request);
  }

  @PostMapping("/prepare/cluster-order")
  public void createClusterOrder(@RequestBody ClusterOrderRequest request) {
    log.info("Received request: {}", request);
    this.selectorService.createClusterOrder(request.getType(), request.getSeed());
  }

  @DeleteMapping("/prepare/cluster-order")
  public void deleteClusterOrder(@RequestBody ClusterOrderRequest request) {
    log.info("Received request: {}", request);
    this.selectorService.deleteClusterOrder(request.getType(), request.getSeed());
  }

  @PostMapping("/select")
  public TaggedDatasetDto select(@RequestBody UsvrSelectionConfig configuration) {
    return this.selectorService.generate(configuration);
  }

  @PostMapping("/clusters")
  public List<RecordCluster> retrieveClusters(@RequestBody UsvrSelectionConfig configuration) {
    return this.selectorService.getClusters(configuration);
  }

  @GetMapping("/configs/example/{name}")
  public UsvrSelectionConfig exampleUsvrSelectionConfig(@PathVariable(required = false) String name) {
    Map<String, Supplier<UsvrSelectionConfig>> examples = Map.of(
            "full", exampleProviderService::createFullExampleConfig,
            "time", exampleProviderService::createTimeExampleConfig,
            "changes", exampleProviderService::createChangesExampleConfig,
            "content", exampleProviderService::createContentFilterExampleConfig
    );
    Supplier<UsvrSelectionConfig> configSupplier = examples.getOrDefault(name, exampleProviderService::createDefaultExampleConfig);
    return configSupplier.get();
  }
}
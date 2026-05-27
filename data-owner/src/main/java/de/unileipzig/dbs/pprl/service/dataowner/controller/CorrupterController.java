package de.unileipzig.dbs.pprl.service.dataowner.controller;

import de.unileipzig.dbs.pprl.service.dataowner.data.dto.*;
import de.unileipzig.dbs.pprl.service.dataowner.generator.DataSetGeneratorConfig;
import de.unileipzig.dbs.pprl.service.dataowner.services.CorrupterService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Set;


@RestController("datasetCorrupterController")
@RequestMapping(value = "generate/corrupter", produces = MediaType.APPLICATION_JSON_VALUE)
@Timed
@Tag(name = CorrupterController.TAG, description = "Generate corrupted records from the database")
@CrossOrigin
@Slf4j
public class CorrupterController {

  public static final String TAG = "Corrupter";

  private final CorrupterService corrupterService;

  public CorrupterController(CorrupterService corrupterService) {
    this.corrupterService = corrupterService;
  }

  @Operation(summary = "Corrupt dataset", tags = TAG)
  @PostMapping("")
  public long corruptDataset(@RequestBody DatasetCorruptionRequestDto corruptionRequestDto) {
    return corrupterService.corruptDataset(corruptionRequestDto);
  }

  @Operation(summary = "Create dataset generation config", tags = TAG)
  @PostMapping("/configs")
  public DataSetGeneratorConfig getDatasetGenerationConfig(@RequestBody DatasetGenerationConfigCreatorDto configDto) {
    return corrupterService.getConfig(configDto);
  }

  @Operation(summary = "Get dataset generation config methods", tags = TAG)
  @GetMapping("/configs/findAll")
  public Set<String> getDatasetGenerationMethods() {
    return corrupterService.getConfigNames();
  }

}
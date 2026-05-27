package de.unileipzig.dbs.pprl.service.generator.controller;

import de.unileipzig.dbs.pprl.service.generator.data.dto.GermanyGeneratorConfig;
import de.unileipzig.dbs.pprl.service.generator.data.dto.TaggedDatasetDto;
import de.unileipzig.dbs.pprl.service.generator.services.GermanyGeneratorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(value = "generator", produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@CrossOrigin
@Tag(name = "Data Generator", description = "API for generating synthetic data records")
public class GeneratorController {

  private final GermanyGeneratorService generatorService;

  public GeneratorController(GermanyGeneratorService generatorService) {
    this.generatorService = generatorService;
  }

  @PostMapping("/generate")
  @Operation(
      summary = "Generate synthetic records",
      description = "Generates a list of synthetic records based on the provided configuration"
  )
  public TaggedDatasetDto generate(
      @Parameter(description = "Configuration for data generation", required = true)
      @RequestBody GermanyGeneratorConfig configuration) {
    return generatorService.generate(configuration);
  }


  @GetMapping("/configs/example/{name}")
  @Operation(
      summary = "Get example configuration",
      description = "Retrieves an example GermanyGeneratorConfiguration with specified parameters",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Successfully retrieved example configuration",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = GermanyGeneratorConfig.class)
              )
          )
      }
  )
  public GermanyGeneratorConfig exampleGermanyGeneratorConfiguration(
      @Parameter(description = "Name of the example configuration (embed 'HH' to include households structures)")
      @PathVariable(required = false) String name) {
    boolean includeHouseholds = false;
    if (name != null && name.contains("HH")) {
      includeHouseholds = true;
    }
    return generatorService.getGermanyGeneratorConfiguration(10000, includeHouseholds);
  }
}

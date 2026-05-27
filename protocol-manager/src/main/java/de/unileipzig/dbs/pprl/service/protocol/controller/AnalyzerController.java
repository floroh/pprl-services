package de.unileipzig.dbs.pprl.service.protocol.controller;

import de.unileipzig.dbs.pprl.core.common.model.impl.SerializableTable;
import de.unileipzig.dbs.pprl.service.protocol.model.dto.ProtocolAnalysisRequestDto;
import de.unileipzig.dbs.pprl.service.protocol.service.AnalyzerService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@Tag(name = AnalyzerController.TAG, description = "Analysis utilities for linkage protocols")
@RequestMapping(value = "analyzer", produces = MediaType.APPLICATION_JSON_VALUE)
@Timed
public class AnalyzerController {

  public static final String TAG = "Protocol analyzer";

  private final AnalyzerService analyzerService;

  public AnalyzerController(AnalyzerService analyzerService) {
      this.analyzerService = analyzerService;
  }

  @Operation(summary = "Get all tags for an executed protocol", tags = TAG)
  @PostMapping("/tag-collection")
  public Collection<de.unileipzig.dbs.pprl.core.common.monitoring.Tag> getTagsFromProtocol(@RequestBody ProtocolAnalysisRequestDto analysisRequestDto) {
    return analyzerService.fetchTags(analysisRequestDto);
  }

  @Operation(summary = "Get all tags for an executed protocol in table format", tags = TAG)
  @PostMapping("/tag-table")
  public SerializableTable getTagsFromProtocolAsTable(
          @RequestBody ProtocolAnalysisRequestDto analysisRequestDto) {
    return analyzerService.fetchTagsAsTable(analysisRequestDto);
  }

}

package de.unileipzig.dbs.pprl.service.linkageunit.controller;

import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.BatchMatchRequestDto;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.ClusteringRequestDto;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.MatchResultDto;
import de.unileipzig.dbs.pprl.service.linkageunit.services.TransientBatchMatcherService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = BatchMatcherController.TAG, description = "Batch matching without persistence")
@RequestMapping(value = "batch", produces = MediaType.APPLICATION_JSON_VALUE)
@Timed
public class BatchMatcherController {

  public static final String TAG = "Transient batch matching";

  private final TransientBatchMatcherService matcherService;

  public BatchMatcherController(TransientBatchMatcherService matcherService) {
    this.matcherService = matcherService;
  }

  @Operation(summary = "Batch matcher", tags = TAG)
  @PostMapping("/match")
  public MatchResultDto match(@RequestBody BatchMatchRequestDto requestDto) {
    return matcherService.match(requestDto);
  }

  @Operation(summary = "Cluster and assign global IDs based on precomputed links", tags = TAG)
  @PostMapping("/cluster")
  public MatchResultDto cluster(@RequestBody ClusteringRequestDto requestDto) {
    return matcherService.cluster(requestDto);
  }

}

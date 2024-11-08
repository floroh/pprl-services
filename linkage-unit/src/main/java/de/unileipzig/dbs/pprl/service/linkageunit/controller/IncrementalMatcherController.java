package de.unileipzig.dbs.pprl.service.linkageunit.controller;

import de.unileipzig.dbs.pprl.service.common.data.dto.RecordDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordIdDto;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.SearchResultDto;
import de.unileipzig.dbs.pprl.service.linkageunit.services.IncrementalMatcherService;
import io.micrometer.core.annotation.Timed;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@Timed
public class IncrementalMatcherController {

  private final IncrementalMatcherService incrementalMatcherService;

  public IncrementalMatcherController(IncrementalMatcherService incrementalMatcherService) {
    this.incrementalMatcherService = incrementalMatcherService;
  }

  @PostMapping("/insert")
  public RecordIdDto create(@RequestBody RecordDto record) {
    return incrementalMatcherService.insert(record);
  }

  @PostMapping("/search")
  public SearchResultDto search(@RequestBody RecordDto record) {
    return incrementalMatcherService.search(record);
  }
}

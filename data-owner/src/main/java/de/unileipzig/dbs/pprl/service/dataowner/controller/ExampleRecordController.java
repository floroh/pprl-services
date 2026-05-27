package de.unileipzig.dbs.pprl.service.dataowner.controller;

import de.unileipzig.dbs.pprl.service.common.data.dto.RecordDto;
import de.unileipzig.dbs.pprl.service.common.services.ExampleRecordGeneratorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@CrossOrigin
public class ExampleRecordController {

  private final ExampleRecordGeneratorService exampleRecordGeneratorService;

  public ExampleRecordController(ExampleRecordGeneratorService exampleRecordGeneratorService) {
    this.exampleRecordGeneratorService = exampleRecordGeneratorService;
  }

  // For Test purposes only
  @GetMapping("/example")
  public RecordDto example() {
    log.debug("Query for example record");
    RecordDto address = exampleRecordGeneratorService.getPlainRecord(true);
    log.debug(address.toString());
    return address;
  }
}
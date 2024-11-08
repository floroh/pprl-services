package de.unileipzig.dbs.pprl.service.protocol.controller;

import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.BatchMatchRequestDto;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.MatchResultDto;
import de.unileipzig.dbs.pprl.service.protocol.model.dto.DatasetCsvDto;
import de.unileipzig.dbs.pprl.service.protocol.service.DataOwnerService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = DataOwnerController.TAG, description = "Adding plaintext dataset, configurations etc. to the" +
  " Data Owner Service")
@RequestMapping(value = "data-owner", produces = MediaType.APPLICATION_JSON_VALUE)
@Timed
public class DataOwnerController {

  public static final String TAG = "Data Owner Preparation";

  private final DataOwnerService dataOwnerService;

  public DataOwnerController(DataOwnerService dataOwnerService) {
    this.dataOwnerService = dataOwnerService;
  }

  @Operation(summary = "Insert dataset from csv file", tags = TAG)
  @PostMapping("/record")
  public int insertFromCsv(@RequestBody DatasetCsvDto datasetCsvDto) {
    dataOwnerService.getRecordInserter().insertDatasetFromCsv(datasetCsvDto.getPath(),
      datasetCsvDto.getDatasetId());
    return datasetCsvDto.getDatasetId();
  }

}

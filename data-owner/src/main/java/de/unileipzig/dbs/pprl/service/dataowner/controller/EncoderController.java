package de.unileipzig.dbs.pprl.service.dataowner.controller;

import de.unileipzig.dbs.pprl.service.common.data.dto.RecordDto;
import de.unileipzig.dbs.pprl.service.dataowner.data.dto.MultiRecordEncodingRequestDto;
import de.unileipzig.dbs.pprl.service.dataowner.data.dto.EncodingRequestDto;
import de.unileipzig.dbs.pprl.service.dataowner.services.EncoderService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@Timed
@Slf4j
@Tag(name = EncoderController.TAG, description = "Encode records provided with the request")
@CrossOrigin
public class EncoderController {

  public static final String TAG = "On-the-fly encoder";

  private final EncoderService encoderService;

  public EncoderController(EncoderService encoderService) {
    this.encoderService = encoderService;
  }

  @Operation(summary = "Encode a single record", tags = TAG)
  @PostMapping("/encode")
  public RecordDto encode(@RequestBody EncodingRequestDto request) {
    return encoderService.encode(request);
  }

  @Operation(summary = "Encode multiple records with the same encoding", tags = TAG)
  @PostMapping("/encode-multiple-same")
  public List<RecordDto> encodeMultipleSame(@RequestBody MultiRecordEncodingRequestDto request) {
    log.info("Received bulk request with " + request.getRecords().size() + " records");
    return encoderService.encode(request);
  }
}
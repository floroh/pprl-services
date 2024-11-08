package de.unileipzig.dbs.pprl.service.dataowner.controller;

import de.unileipzig.dbs.pprl.service.common.data.dto.RecordDto;
import de.unileipzig.dbs.pprl.service.common.services.DatasetDtoService;
import de.unileipzig.dbs.pprl.service.dataowner.data.dto.MultiRecordEncodingRequestDto;
import de.unileipzig.dbs.pprl.service.dataowner.data.dto.MultiRecordEncodingRetrievalRequestDto;
import de.unileipzig.dbs.pprl.service.dataowner.data.dto.EncodingRequestDto;
import de.unileipzig.dbs.pprl.service.dataowner.data.dto.EncodingRetrievalRequestDto;
import de.unileipzig.dbs.pprl.service.dataowner.services.EncoderService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController("dataOwnerRecordController")
@RequestMapping(value = "record", produces = MediaType.APPLICATION_JSON_VALUE)
@Timed
@Tag(name = EncodedRecordController.TAG, description = "Encode records from the database")
@CrossOrigin
@Slf4j
public class EncodedRecordController {

  public static final String TAG = "Encoder";

  private final DatasetDtoService datasetDtoService;

  private final EncoderService encoderService;

  public EncodedRecordController(DatasetDtoService datasetDtoService, EncoderService encoderService) {
    this.datasetDtoService = datasetDtoService;
    this.encoderService = encoderService;
  }

  @Operation(summary = "Encode a persisted record", tags = TAG)
  @PostMapping("/{uniqueId}/encode")
  public RecordDto encode(@PathVariable String uniqueId, @RequestBody EncodingRequestDto encodingRequestDto) {
    RecordDto recordDto = datasetDtoService.getRecordAsDto(uniqueId);
    encodingRequestDto.setRecord(recordDto);
    return encoderService.encode(encodingRequestDto);
  }

  @Operation(summary = "Bundled encoding requests for multiple persisted records", tags = TAG)
  @PostMapping("/encode-multiple")
  public List<RecordDto> encodeMultiple(@RequestBody List<EncodingRetrievalRequestDto> encodingRequestList) {
    log.info("Received encoding request for {} records", encodingRequestList.size());
    List<RecordDto> encodedRecords = new ArrayList<>();
    int c = 0;
    Map<String, RecordDto> recordsByUniqueLikeId;
    long pre = System.currentTimeMillis();
    //TODO Fix assumption that all records are in the same dataset
    if (encodingRequestList.size() > 50) {
      recordsByUniqueLikeId =
        datasetDtoService.getAllRecordsAsDto(encodingRequestList.getFirst().getDatasetId()).stream()
          .collect(Collectors.toMap(r -> r.getId().getUniqueLike(), r -> r));
    } else {
      recordsByUniqueLikeId =
        datasetDtoService.getRecordsAsDto(
            encodingRequestList.getFirst().getDatasetId(),
            encodingRequestList.stream().map(EncodingRetrievalRequestDto::getRecordId)
              .collect(Collectors.toList())
          ).stream()
          .collect(Collectors.toMap(r -> r.getId().getUniqueLike(), r -> r));
    }
    long post = System.currentTimeMillis();
    log.info("Retrieved {} records in {} ms", recordsByUniqueLikeId.size(), post - pre);
    for (EncodingRetrievalRequestDto requestDto : encodingRequestList) {
      RecordDto recordAsDto = recordsByUniqueLikeId.get(requestDto.getRecordId().getUniqueLike());
      if (recordAsDto == null) {
        log.warn(
          "Could not find record in dataset {} with id {}: Skipping it...",
          requestDto.getDatasetId(),
          requestDto.getRecordId()
        );
        continue;
      }
      EncodingRequestDto encodingRequest = EncodingRequestDto.builder()
        .record(recordAsDto)
        .encodingId(requestDto.getEncodingId())
        .recordSecret(requestDto.getRecordSecret())
        .build();
      RecordDto encodedRecord = encoderService.encode(encodingRequest);
      encodedRecord.setId(requestDto.getRecordId());
      encodedRecords.add(encodedRecord);
      c++;
      if (c % 100 == 0) {
        log.info("Encoded {} records", c);
      }
    }
    return encodedRecords;
  }

  @Operation(summary = "Encode multiple persisted records with the same encoding", tags = TAG)
  @PostMapping("/encode-multiple-same")
  public List<RecordDto> encodeMultipleSame(@RequestBody MultiRecordEncodingRetrievalRequestDto requestDto) {
    log.info("Received bulk encoding request for encoding {}", requestDto.getEncodingId());
    List<RecordDto> allRecordsAsDto;
    if (requestDto.getRecordIds() != null && !requestDto.getRecordIds().isEmpty()) {
      allRecordsAsDto = requestDto.getRecordIds().stream()
        .map(rid -> datasetDtoService.getRecordAsDto(requestDto.getDatasetId(), rid))
        .collect(Collectors.toList());
    } else {
      allRecordsAsDto = datasetDtoService.getAllRecordsAsDto(requestDto.getDatasetId());
    }
    log.info("Encoding {} records", allRecordsAsDto.size());
    return encoderService.encode(MultiRecordEncodingRequestDto.builder()
      .encodingId(requestDto.getEncodingId())
      .records(allRecordsAsDto)
      .build());
//    return allRecordsAsDto.stream()
//      .map(r -> new EncodingRequestDto(requestDto.getEncodingId(), r, null))
//      .map(encoderService::encode)
//      .collect(Collectors.toList());
  }

}
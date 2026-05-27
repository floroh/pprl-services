package de.unileipzig.dbs.pprl.service.protocol.api;

import de.unileipzig.dbs.pprl.service.common.data.dto.EncodingDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.EncodingIdDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordDto;
import de.unileipzig.dbs.pprl.service.dataowner.data.dto.EncodingCreationRequestDto;
import de.unileipzig.dbs.pprl.service.dataowner.data.dto.EncodingCreationResponseDto;
import de.unileipzig.dbs.pprl.service.dataowner.data.dto.EncodingRetrievalRequestDto;
import de.unileipzig.dbs.pprl.service.dataowner.data.dto.MultiRecordEncodingRetrievalRequestDto;
import kong.unirest.Unirest;

import java.util.Arrays;
import java.util.List;

public class EncoderApi extends CommonApi {

  public EncoderApi() {
    super("http://localhost:8081");
  }

  public List<RecordDto> fetchPlain(long datasetId) {
    String endPoint = url + "/record/" + datasetId + "/all";
    RecordDto[] records = Unirest.get(endPoint)
      .asObject(RecordDto[].class)
      .getBody();
    return Arrays.asList(records);
  }

  public List<RecordDto> retrieveEncoded(long datasetId, EncodingIdDto encodingIdDto) {
    String endPoint = url + "/record/encode-multiple-same";
    RecordDto[] records = Unirest.post(endPoint)
      .body(MultiRecordEncodingRetrievalRequestDto.builder()
        .encodingId(encodingIdDto)
        .datasetId(datasetId)
        .build())
      .asObject(RecordDto[].class)
      .getBody();
    return Arrays.asList(records);
  }

  public List<RecordDto> retrieveMultipleEncoded(List<EncodingRetrievalRequestDto> retrievalRequestDtos) {
    String endPoint = url + "/record/encode-multiple";
    RecordDto[] records = Unirest.post(endPoint)
      .body(retrievalRequestDtos)
      .asObject(RecordDto[].class)
      .getBody();
    return Arrays.asList(records);
  }

  public EncodingCreationResponseDto createEncoding(EncodingCreationRequestDto requestDto) {
    String endPoint = url + "/config/create";
    return Unirest.post(endPoint)
      .body(requestDto)
      .asObject(EncodingCreationResponseDto.class)
      .getBody();
  }

}

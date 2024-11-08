package de.unileipzig.dbs.pprl.service.protocol.api;

import de.unileipzig.dbs.pprl.service.common.data.dto.EncodingIdDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordDto;
import de.unileipzig.dbs.pprl.service.dataowner.data.dto.MultiRecordEncodingRetrievalRequestDto;
import de.unileipzig.dbs.pprl.service.dataowner.data.dto.EncodingRetrievalRequestDto;
import de.unileipzig.dbs.pprl.service.protocol.csv.JacksonObjectMapper;
import kong.unirest.Unirest;

import java.util.Arrays;
import java.util.List;

public class EncoderApi {

  public static String encoderUrl = "http://localhost:8081";
  
  public EncoderApi() {
    Unirest.config().reset();
    Unirest.config()
      .addDefaultHeader("Content-Type", "application/json")
      .setObjectMapper(new JacksonObjectMapper());
  }

  public static void setUrl(String url) {
    EncoderApi.encoderUrl = url;
  }

  public void delete(int datasetId) {
    String endPoint = encoderUrl + "/record/" + datasetId + "/all";
    Unirest.delete(endPoint)
      .asString();
  }

  public List<RecordDto> fetchPlain(int datasetId) {
    String endPoint = encoderUrl + "/record/" + datasetId + "/all";
    RecordDto[] records = Unirest.get(endPoint)
      .asObject(RecordDto[].class)
      .getBody();
    return Arrays.asList(records);
  }


  public List<RecordDto> retrievePlain(int datasetId) {
    String endPoint = encoderUrl + "/record/" + datasetId + "/all";
    RecordDto[] records = Unirest.get(endPoint)
      .asObject(RecordDto[].class)
      .getBody();
    return Arrays.asList(records);
  }

  public List<RecordDto> retrieveEncoded(int datasetId, EncodingIdDto encodingIdDto) {
    String endPoint = encoderUrl + "/record/encode-multiple-same";
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
    String endPoint = encoderUrl + "/record/encode-multiple";
    RecordDto[] records = Unirest.post(endPoint)
      .body(retrievalRequestDtos)
      .asObject(RecordDto[].class)
      .getBody();
    return Arrays.asList(records);
  }

}

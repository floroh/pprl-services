package de.unileipzig.dbs.pprl.service.common.data.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"id", "datasetId", "encodingId", "attributes"})
public class RecordDto {

  private RecordIdDto id;

  private int datasetId;

  private EncodingIdDto encodingId;

  @Singular
  @JsonPropertyOrder(alphabetic = true)
  private Map<String, AttributeDto> attributes;

}

package de.unileipzig.dbs.pprl.service.common.data.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DatasetDto {

  private Long datasetId;

  private String datasetName;

  private Long plaintextDatasetId;

  private EncodingIdDto encodingIdDto;

  @Builder.Default
  private Map<String, String> properties = new HashMap<>();
}

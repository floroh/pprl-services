package de.unileipzig.dbs.pprl.service.protocol.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProtocolAnalysisRequestDto {

  private String protocolId;

  private String type;

  @Builder.Default
  private Map<String, String> parameters = new HashMap<>();

}

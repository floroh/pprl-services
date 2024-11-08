package de.unileipzig.dbs.pprl.service.protocol.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class ProtocolExecutionDto {

  private String protocolId;

  private int numberOfSteps = -1;

  private String stepToStop;
}

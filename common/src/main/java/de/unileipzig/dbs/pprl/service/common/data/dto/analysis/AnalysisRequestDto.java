package de.unileipzig.dbs.pprl.service.common.data.dto.analysis;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnalysisRequestDto {

  private int datasetId;

  private String projectId;

  private String type;

  private Map<String, String> parameters = new HashMap<>();

}

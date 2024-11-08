package de.unileipzig.dbs.pprl.service.linkageunit.data.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.unileipzig.dbs.pprl.service.common.data.dto.analysis.AnalysisRequestDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MatchResultAnalysisRequestDto {

  private AnalysisRequestDto analysisRequest;

  private MatchResultDto matchResult;

}

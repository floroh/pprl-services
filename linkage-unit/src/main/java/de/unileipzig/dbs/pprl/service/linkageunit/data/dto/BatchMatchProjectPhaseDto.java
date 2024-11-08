package de.unileipzig.dbs.pprl.service.linkageunit.data.dto;

import de.unileipzig.dbs.pprl.service.common.data.dto.analysis.AnalysisResultDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.reporting.ReportGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchMatchProjectPhaseDto {

  /**
   * Name of the phase, e.g. BLOCKING or LINKING
   */
  private String name;

  /**
   * True, if phase is finished
   */
  private boolean isDone;

  /**
   * Progress between 0 and 1
   */
  private double progress;

  /**
   * Optional reports on this phase, e.g. metrics describing the distribution of similarity scores in the
   * linking phase
   */
  private Map<String, AnalysisResultDto> reports;

  private List<ReportGroup> reportGroups;

}

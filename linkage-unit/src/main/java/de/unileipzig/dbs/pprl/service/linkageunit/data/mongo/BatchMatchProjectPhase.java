package de.unileipzig.dbs.pprl.service.linkageunit.data.mongo;

import de.unileipzig.dbs.pprl.service.common.data.dto.reporting.ReportGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchMatchProjectPhase {

  private PhaseProgress phaseProgress;

  @Singular
  private Map<String, ReportGroup> reportGroups;

}

package de.unileipzig.dbs.pprl.service.common.data.dto.analysis;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.unileipzig.dbs.pprl.service.common.data.dto.reporting.ReportGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnalysisResultDto {

  @NonNull
  private String name;

  private String description;

  private Map<String, ReportGroup> reportGroups;

  public Optional<ReportGroup> getReportGroup(String reportGroupName) {
    return Optional.ofNullable(reportGroups.get(reportGroupName));
  }

  public static class AnalysisResultDtoBuilder {
    private Map<String, ReportGroup> reportGroups;

    public AnalysisResultDtoBuilder reportGroup(ReportGroup reportGroup) {
      if (reportGroups == null) {
        reportGroups = new LinkedHashMap<>();
      }
      reportGroups.put(reportGroup.getName(), reportGroup);
      return this;
    }
  }
}

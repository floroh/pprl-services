package de.unileipzig.dbs.pprl.service.common.data.dto.reporting;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportGroup {

  private String name;

  private Map<String, Report> reports;

  public Optional<Report> getReport(String reportName) {
    return Optional.ofNullable(reports.get(reportName));
  }

  public static class ReportGroupBuilder {
    private Map<String, Report> reports = new LinkedHashMap<>();

    public ReportGroupBuilder report(Report report) {
      if (reports == null) {
        reports = new LinkedHashMap<>();
      }
      reports.put(report.getName(), report);
      return this;
    }
  }
}

package de.unileipzig.dbs.pprl.service.common.data.dto.reporting;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportGroup {

  private String name;

  private Map<String, Report> reports;

  public static class ReportGroupBuilder {
    private Map<String, Report> reports = new HashMap<>();

    public ReportGroupBuilder report(Report report) {
      if (reports == null) {
        reports = new HashMap<>();
      }
      reports.put(report.getName(), report);
      return this;
    }
  }
}

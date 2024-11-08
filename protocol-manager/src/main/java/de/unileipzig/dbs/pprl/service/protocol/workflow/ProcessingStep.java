package de.unileipzig.dbs.pprl.service.protocol.workflow;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.unileipzig.dbs.pprl.service.common.data.dto.reporting.ReportGroup;
import de.unileipzig.dbs.pprl.service.linkageunit.data.mongo.PhaseProgress;
import de.unileipzig.dbs.pprl.service.protocol.service.MultiLayerProtocolRunner;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Data
@SuperBuilder
@AllArgsConstructor
public class ProcessingStep {

  public interface StepType {
  }

  protected String type;

  @Singular
  protected Map<String, String> properties;

  protected PhaseProgress phaseProgress;

  @Singular
  protected Map<String, ReportGroup> reportGroups;

  public ProcessingStep() {
    this.properties = new HashMap<>();
    this.reportGroups = new HashMap<>();
  }

  public Map<String, String> getProperties() {
    if (properties == null || properties.isEmpty()) {
      properties = new HashMap<>();
    } else if (!(properties instanceof HashMap)) {
      properties = new HashMap<>(properties);
    }
    return properties;
  }

  public Map<String, ReportGroup> getReportGroups() {
    if (reportGroups == null || reportGroups.isEmpty()) {
      reportGroups = new HashMap<>();
    } else if (!(reportGroups instanceof HashMap)) {
      reportGroups = new HashMap<>(reportGroups);
    }
    return reportGroups;
  }

  @JsonIgnore
  public ProcessingStep getAsUntypedStep() {
    return ProcessingStep.builder()
      .type(this.getType())
      .properties(this.properties)
      .phaseProgress(this.phaseProgress)
      .reportGroups(this.reportGroups)
      .build();
  }

  @JsonIgnore
  public void setTypeFromEnum(StepType type) {
    this.type = type.toString();
  }

  public void execute(MultiLayerProtocolRunner runner) {
    phaseProgress = PhaseProgress.builder()
      .done(false)
      .progress(0)
      .description(getType().toString())
      .build();
  }

}


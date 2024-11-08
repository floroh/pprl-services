package de.unileipzig.dbs.pprl.service.protocol.workflow.steps;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.unileipzig.dbs.pprl.service.common.data.dto.reporting.ReportGroup;
import de.unileipzig.dbs.pprl.service.linkageunit.data.mongo.PhaseProgress;
import de.unileipzig.dbs.pprl.service.protocol.service.MultiLayerProtocolRunner;
import de.unileipzig.dbs.pprl.service.protocol.workflow.MultiLayerActiveLearningWorkflow;
import de.unileipzig.dbs.pprl.service.protocol.workflow.ProcessingStep;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class ReclassifyStep extends ProcessingStep {

  public ReclassifyStep() {
    this.type = getStepType().toString();
  }

  public ReclassifyStep(String type, Map<String, String> properties,
    PhaseProgress phaseProgress,
    Map<String, ReportGroup> reportGroups) {
    super(type, properties, phaseProgress, reportGroups);
    this.type = getStepType().toString();
  }

  public static StepType getStepType() {
    return MultiLayerActiveLearningWorkflow.StepType.RECLASSIFY_PAIRS;
  }

  public String getType() {
    if (super.getType() == null) {
      setTypeFromEnum(getStepType());
    }
    return super.getType();
  }

  @Override
  public void execute(MultiLayerProtocolRunner runner) {
    super.execute(runner);
    String projectId = getProjectId();
    runner.getMatcher().reclassify(projectId);
    runner.fetchProject(projectId, true);
    phaseProgress.setProgress(1.0);
    phaseProgress.setDone(true);
  }

  @JsonIgnore
  public String getProjectId() {
    return getProperties().get("projectId");
  }

  public ReclassifyStep setProjectId(String projectId) {
    getProperties().put("projectId", projectId);
    return this;
  }

  @Override
  public String toString() {
    return "ReclassifyStep{" +
      "properties=" + properties +
      "} " + super.toString();
  }
}



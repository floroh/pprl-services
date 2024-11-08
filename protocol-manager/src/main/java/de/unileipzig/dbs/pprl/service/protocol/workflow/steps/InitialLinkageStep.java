package de.unileipzig.dbs.pprl.service.protocol.workflow.steps;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.unileipzig.dbs.pprl.service.common.data.dto.reporting.ReportGroup;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.BatchMatchProjectDto;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.MatchingDto;
import de.unileipzig.dbs.pprl.service.linkageunit.data.mongo.PhaseProgress;
import de.unileipzig.dbs.pprl.service.protocol.model.mongo.Layer;
import de.unileipzig.dbs.pprl.service.protocol.service.MultiLayerProtocolRunner;
import de.unileipzig.dbs.pprl.service.protocol.workflow.MultiLayerActiveLearningWorkflow;
import de.unileipzig.dbs.pprl.service.protocol.workflow.ProcessingStep;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.Map;

import static de.unileipzig.dbs.pprl.service.protocol.workflow.steps.UpdateMatcherStep.addRbfThresholdProperty;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class InitialLinkageStep extends ProcessingStep {

  public InitialLinkageStep() {
    super();
    this.type = getStepType().toString();
  }

  public InitialLinkageStep(String type, Map<String, String> properties,
    PhaseProgress phaseProgress,
    Map<String, ReportGroup> reportGroups) {
    super(type, properties, phaseProgress, reportGroups);
    this.type = getStepType().toString();
  }

  public static StepType getStepType() {
    return MultiLayerActiveLearningWorkflow.StepType.RUN_INITIAL_LINKAGE;
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
    Layer layer = runner.getProtocol().getLayers().getFirst();
    BatchMatchProjectDto project = runner.getMatcher().continueProject(getProjectId());
    MatchingDto matchingDto = runner.getMatcher().fetchMatching(project.getMethod());
    addRbfThresholdProperty(matchingDto, this);
    layer.setProject(project);
    phaseProgress.setProgress(1.0);
    phaseProgress.setDone(true);
  }

  public InitialLinkageStep setProjectId(String projectId) {
    getProperties().put("projectId", projectId);
    return this;
  }

  @JsonIgnore
  public String getProjectId() {
    return getProperties().get("projectId");
  }

  @Override
  public String toString() {
    return "InitialLinkageStep{" +
      "properties=" + properties +
      "} " + super.toString();
  }
}



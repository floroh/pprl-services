package de.unileipzig.dbs.pprl.service.protocol.workflow.steps;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.unileipzig.dbs.pprl.service.common.data.dto.reporting.ReportGroup;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.RecordPairDto;
import de.unileipzig.dbs.pprl.service.linkageunit.data.mongo.PhaseProgress;
import de.unileipzig.dbs.pprl.service.protocol.service.MultiLayerProtocolRunner;
import de.unileipzig.dbs.pprl.service.protocol.workflow.MultiLayerActiveLearningWorkflow;
import de.unileipzig.dbs.pprl.service.protocol.workflow.ProcessingStep;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class DetermineUncertainLinksStep extends ProcessingStep {

  public DetermineUncertainLinksStep() {
    this.type = getStepType().toString();
  }

  public DetermineUncertainLinksStep(String type, Map<String, String> properties,
    PhaseProgress phaseProgress,
    Map<String, ReportGroup> reportGroups) {
    super(type, properties, phaseProgress, reportGroups);
    this.type = getStepType().toString();
  }

  public static StepType getStepType() {
    return MultiLayerActiveLearningWorkflow.StepType.DETERMINE_UNCERTAIN_LINKS;
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
    List<RecordPairDto> pairs = runner.getMatcher().determineUncertainLinks(getProjectId());
    setNumberOfUncertainLinks(pairs.size());
    phaseProgress.setProgress(1.0);
    phaseProgress.setDone(true);
  }

  @JsonIgnore
  public String getProjectId() {
    return getProperties().get("projectId");
  }

  public DetermineUncertainLinksStep setProjectId(String projectId) {
    getProperties().put("projectId", projectId);
    return this;
  }

  @JsonIgnore
  public int getNumberOfUncertainLinks () {
    return Integer.parseInt(getProperties().get("numberOfUncertainLinks"));
  }

  public void setNumberOfUncertainLinks(int numberOfUncertainLinks) {
    getProperties().put("numberOfUncertainLinks", String.valueOf(numberOfUncertainLinks));
  }

  @Override
  public String toString() {
    return "DetermineUncertainLinksStep{" +
      "properties=" + properties +
      "} " + super.toString();
  }
}



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
public class IdleStep extends ProcessingStep {

  public IdleStep() {
    super();
    this.type = getStepType().toString();
  }

  public IdleStep(String type, Map<String, String> properties,
    PhaseProgress phaseProgress,
    Map<String, ReportGroup> reportGroups) {
    super(type, properties, phaseProgress, reportGroups);
    this.type = getStepType().toString();
  }

  public static StepType getStepType() {
    return MultiLayerActiveLearningWorkflow.StepType.IDLE;
  }

  public String getType() {
    if (super.getType() == null) {
      setTypeFromEnum(getStepType());
    }
    return super.getType();
  }

  @Override
  public void execute(MultiLayerProtocolRunner runner) {
  }

  public IdleStep setDescription(String description) {
    getProperties().put("description", description);
    return this;
  }

  @JsonIgnore
  public String getDescription() {
    return getProperties().get("description");
  }


  @Override
  public String toString() {
    return "IdleStep{" +
      "properties=" + properties +
      "} " + super.toString();
  }
}



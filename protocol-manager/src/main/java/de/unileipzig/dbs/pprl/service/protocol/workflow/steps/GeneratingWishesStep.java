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
public class GeneratingWishesStep extends ProcessingStep {

  public GeneratingWishesStep() {
    this.type = getStepType().toString();
  }

  public GeneratingWishesStep(String type, Map<String, String> properties,
    PhaseProgress phaseProgress,
    Map<String, ReportGroup> reportGroups) {
    super(type, properties, phaseProgress, reportGroups);
    this.type = getStepType().toString();
  }

  public static StepType getStepType() {
    return MultiLayerActiveLearningWorkflow.StepType.GENERATE_WISHES;
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
    runner.getMatcher().getRecordEncodingWishes(getProjectId(), getWishLimit());
    phaseProgress.setProgress(1.0);
    phaseProgress.setDone(true);
  }

  @JsonIgnore
  public String getProjectId() {
    return getProperties().get("projectId");
  }

  @JsonIgnore
  public int getWishLimit() {
    return Integer.parseInt(getProperties().get("wishLimit"));
  }

}



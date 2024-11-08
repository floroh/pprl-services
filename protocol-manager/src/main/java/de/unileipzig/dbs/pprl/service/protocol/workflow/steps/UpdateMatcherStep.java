package de.unileipzig.dbs.pprl.service.protocol.workflow.steps;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.unileipzig.dbs.pprl.service.common.data.dto.reporting.ReportGroup;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.MatchingDto;
import de.unileipzig.dbs.pprl.service.linkageunit.data.mongo.PhaseProgress;
import de.unileipzig.dbs.pprl.service.protocol.model.mongo.Layer;
import de.unileipzig.dbs.pprl.service.protocol.service.MultiLayerProtocolRunner;
import de.unileipzig.dbs.pprl.service.protocol.utils.JsonModifier;
import de.unileipzig.dbs.pprl.service.protocol.workflow.MultiLayerActiveLearningWorkflow;
import de.unileipzig.dbs.pprl.service.protocol.workflow.ProcessingStep;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import net.minidev.json.JSONArray;

import java.util.Map;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class UpdateMatcherStep extends ProcessingStep {


  private static final String JSONPATH_TRAINABLE_THRESHOLD_CLASSIFIER_THRESHOLD = "$.." +
          JsonModifier.classSelector("classifier", "TrainableThresholdClassifier") +
          ".threshold";

  public UpdateMatcherStep() {
    this.type = getStepType().toString();
  }

  public UpdateMatcherStep(String type, Map<String, String> properties,
    PhaseProgress phaseProgress,
    Map<String, ReportGroup> reportGroups) {
    super(type, properties, phaseProgress, reportGroups);
    this.type = getStepType().toString();
  }

  public static StepType getStepType() {
    return MultiLayerActiveLearningWorkflow.StepType.UPDATE_MATCHER;
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
    Layer layer = runner.getProtocol().getLayerOfProject(projectId);
    MatchingDto updatedMatcher = runner.getMatcher().updateMatcher(
      layer.getProjectId(), layer.getUpdateType());
    addRbfThresholdProperty(updatedMatcher, this);
    runner.fetchProject(projectId, true);
    phaseProgress.setProgress(1.0);
    phaseProgress.setDone(true);
  }

  @JsonIgnore
  public String getProjectId() {
    return getProperties().get("projectId");
  }

  public UpdateMatcherStep setProjectId(String projectId) {
    getProperties().put("projectId", projectId);
    return this;
  }

  public static void addRbfThresholdProperty(MatchingDto updatedMatcher, ProcessingStep step) {
    if (updatedMatcher.getId().getMethod().contains("RBF")) {
      step.getProperties().put(
        "matcher.rbf.threshold",
        String.valueOf(getThresholdFromConfig(updatedMatcher.getConfig()))
      );
    }
  }

  public static double getThresholdFromConfig(String config) {
    JSONArray read = (JSONArray) JsonModifier.read(config, JSONPATH_TRAINABLE_THRESHOLD_CLASSIFIER_THRESHOLD);
    return (double)read.get(0);
  }

  @Override
  public String toString() {
    return "UpdateMatcherStep{" +
      "properties=" + properties +
      "} " + super.toString();
  }
}



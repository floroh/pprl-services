package de.unileipzig.dbs.pprl.service.protocol.workflow.steps;

import de.unileipzig.dbs.pprl.service.common.data.dto.reporting.ReportGroup;
import de.unileipzig.dbs.pprl.service.dataowner.data.dto.EncodingCreationRequestDto;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.BatchMatchProjectDto;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.MatcherIdDto;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.MatchingCreationRequestDto;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.MatchingDto;
import de.unileipzig.dbs.pprl.service.linkageunit.data.mongo.PhaseProgress;
import de.unileipzig.dbs.pprl.service.protocol.api.MatcherApi;
import de.unileipzig.dbs.pprl.service.protocol.model.mongo.Layer;
import de.unileipzig.dbs.pprl.service.protocol.service.MultiLayerProtocolRunner;
import de.unileipzig.dbs.pprl.service.protocol.workflow.MultiLayerActiveLearningWorkflow;
import de.unileipzig.dbs.pprl.service.protocol.workflow.ProcessingStep;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class PrepareMatcherConfigs extends ProcessingStep {

  public PrepareMatcherConfigs() {
    this.type = getStepType().toString();
  }

  public PrepareMatcherConfigs(String type, Map<String, String> properties,
                               PhaseProgress phaseProgress,
                               Map<String, ReportGroup> reportGroups) {
    super(type, properties, phaseProgress, reportGroups);
    this.type = getStepType().toString();
  }

  public static StepType getStepType() {
    return MultiLayerActiveLearningWorkflow.StepType.PREPARE_MATCHER_CONFIGS;
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
    for (Layer layer : runner.getProtocol().getLayers()) {
      String attributeWeightMethod = layer.getAttributeWeightMethod();
      if (attributeWeightMethod != null && !attributeWeightMethod.isBlank()) {
        String matcherMethod = layer.getMatcherMethod();
        if (matcherMethod.contains("PT") || matcherMethod.contains("ABF")) {
          log.info("Adapting matching method with attributeWeightMethod={}", attributeWeightMethod);
          String outputMatcherMethod = matcherMethod + "/prj=" + layer.getProjectId();
          runner.fetchProject(layer.getProjectId(), false);
          MatchingCreationRequestDto.WeightSelectionMethod weightSelectionMethod = MatchingCreationRequestDto.WeightSelectionMethod.NONE;
          if (attributeWeightMethod.contains("wFS")) {
            weightSelectionMethod = MatchingCreationRequestDto.WeightSelectionMethod.FELLEGI_SUNTER;
          }
          MatchingCreationRequestDto request = MatchingCreationRequestDto.builder()
                  .weightSelectionMethod(weightSelectionMethod)
                  .baseMatcherId(MatcherIdDto.builder().method(matcherMethod).build())
                  .outputMatcherId(MatcherIdDto.builder().method(outputMatcherMethod).build())
                  .datasetId(layer.getProject().getDatasetId())
                  .persist(true)
                  .build();
          if (layer.getInitialAttributeWeights() != null) {
            log.info("Setting attribute weights: {}", layer.getInitialAttributeWeights());
            request.setAttributeWeights(layer.getInitialAttributeWeights());
            getProperties().put("weight-origin", "layer-config");
          } else {
            getProperties().put("weight-origin", "auto: " + attributeWeightMethod);
          }
          MatcherApi matcherApi = runner.getProtocolService().getLinkageUnitService().getMatcherApi();
          MatchingDto newMatching = matcherApi.createMatching(request);
          layer.setMatcherMethod(newMatching.getId().getMethod());
          BatchMatchProjectDto project = matcherApi.getProject(layer.getProjectId());
          project.setMethod(newMatching.getId().getMethod());
          matcherApi.updateProject(project);
        }
      }
      phaseProgress.setProgress(1.0);
      phaseProgress.setDone(true);
    }
  }

  @Override
  public String toString() {
    return "PrepareProtocolConfigs{" +
      "properties=" + properties +
      "} " + super.toString();
  }
}



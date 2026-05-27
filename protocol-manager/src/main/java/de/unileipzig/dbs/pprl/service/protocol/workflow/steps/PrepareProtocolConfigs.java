package de.unileipzig.dbs.pprl.service.protocol.workflow.steps;

import de.unileipzig.dbs.pprl.service.common.data.dto.EncodingDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.EncodingIdDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.reporting.ReportGroup;
import de.unileipzig.dbs.pprl.service.dataowner.data.dto.EncodingCreationRequestDto;
import de.unileipzig.dbs.pprl.service.dataowner.data.dto.EncodingCreationResponseDto;
import de.unileipzig.dbs.pprl.service.linkageunit.data.mongo.PhaseProgress;
import de.unileipzig.dbs.pprl.service.protocol.model.mongo.Layer;
import de.unileipzig.dbs.pprl.service.protocol.service.MultiLayerProtocolRunner;
import de.unileipzig.dbs.pprl.service.protocol.workflow.MultiLayerActiveLearningWorkflow;
import de.unileipzig.dbs.pprl.service.protocol.workflow.ProcessingStep;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class PrepareProtocolConfigs extends ProcessingStep {

  public PrepareProtocolConfigs() {
    this.type = getStepType().toString();
  }

  public PrepareProtocolConfigs(String type, Map<String, String> properties,
                                PhaseProgress phaseProgress,
                                Map<String, ReportGroup> reportGroups) {
    super(type, properties, phaseProgress, reportGroups);
    this.type = getStepType().toString();
  }

  public static StepType getStepType() {
    return MultiLayerActiveLearningWorkflow.StepType.PREPARE_CONFIGS;
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
        String encodingMethod = layer.getEncodingMethod();
        if (encodingMethod.contains("RBF")) {
          log.info("Adapting RBF encoding method with attributeWeightMethod={}", attributeWeightMethod);
          String outputEncodingMethod = encodingMethod + "/prj=" + layer.getProjectId();
          runner.fetchProject(layer.getProjectId(), false);
          EncodingCreationRequestDto.WeightSelectionMethod weightSelectionMethod = EncodingCreationRequestDto.WeightSelectionMethod.NONE;
          if (attributeWeightMethod.contains("wFS")) {
            weightSelectionMethod = EncodingCreationRequestDto.WeightSelectionMethod.FELLEGI_SUNTER;
          }
          EncodingCreationRequestDto request = EncodingCreationRequestDto.builder()
                  .encodingCreationMethod(EncodingCreationRequestDto.EncodingCreationMethod.BASE_ENCODING_ADAPTION)
                  .weightSelectionMethod(weightSelectionMethod)
                  .frequencySelectionMethod(EncodingCreationRequestDto.FrequencySelectionMethod.DATABASE)
                  .baseEncodingId(EncodingIdDto.builder().method(encodingMethod).build())
                  .outputEncodingId(EncodingIdDto.builder().method(outputEncodingMethod).build())
                  .datasetId(runner.getProtocol().getPlaintextDatasetId())
                  .maxFillrate(0.5f)
                  .bloomFilterSize(1024)
                  .persist(true)
                  .build();
          boolean useSourceSpecificWeights = attributeWeightMethod.contains("sourceSpecific");
          if (useSourceSpecificWeights) {
            getProperties().put("weight-source-specific", "True");
          }
          if (layer.getInitialAttributeWeights() != null) {
            log.info("Setting attribute weights: {}", layer.getInitialAttributeWeights());
            request.setAttributeWeights(layer.getInitialAttributeWeights());
            getProperties().put("weight-origin", "layer-config");
          } else {
            getProperties().put("weight-origin", "auto: " + attributeWeightMethod);
          }
          if (useSourceSpecificWeights) {
            request.setSourceSpecific(true);
            request.setAttributeErrorRates(layer.getAttributeErrorRates());
          }
          EncodingCreationResponseDto response = runner.getProtocolService().getDataOwnerService().getEncoderApi()
                  .createEncoding(request);
          layer.setEncodingMethod(response.getEncoding().getId().getMethod());

          if (useSourceSpecificWeights) {
            Optional.ofNullable(response.getRequest().getSourceSpecificAttributeWeights()).orElse(new HashMap<>())
                    .forEach((s, aws) -> aws.forEach((a, aw) ->
                            getProperties().put("weight." + s + "." + a, String.format(Locale.ENGLISH, "%.2f", aw))));
            Optional.ofNullable(response.getRequest().getSourceSpecificAttributeLength()).orElse(new HashMap<>())
                    .forEach((s, als) -> als.forEach((a, al) ->
                            getProperties().put("length." + s + "." + a, String.format(Locale.ENGLISH, "%.2f", al))));
          } else {
            if (layer.getInitialAttributeWeights() == null) {
              Optional.ofNullable(response.getRequest().getAttributeWeights()).orElse(new HashMap<>())
                      .forEach((a, aw) ->
                              getProperties().put("weight." + a, String.format(Locale.ENGLISH, "%.2f", aw)));
            }
            Optional.ofNullable(response.getRequest().getAttributeLength()).orElse(new HashMap<>())
                    .forEach((a, al) ->
                            getProperties().put("length." + a, String.format(Locale.ENGLISH, "%.2f", al)));
          }
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



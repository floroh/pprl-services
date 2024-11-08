package de.unileipzig.dbs.pprl.service.protocol.service;

import de.unileipzig.dbs.pprl.service.protocol.api.MatcherApi;
import de.unileipzig.dbs.pprl.service.protocol.model.dto.ProtocolExecutionDto;
import de.unileipzig.dbs.pprl.service.protocol.model.mongo.Layer;
import de.unileipzig.dbs.pprl.service.protocol.model.mongo.MultiLayerProtocol;
import de.unileipzig.dbs.pprl.service.protocol.workflow.MultiLayerActiveLearningWorkflow;
import de.unileipzig.dbs.pprl.service.protocol.workflow.ProcessingStep;
import de.unileipzig.dbs.pprl.service.protocol.workflow.ProcessingStepFactory;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Locale;


@Slf4j
public class MultiLayerProtocolRunner {

  @Getter
  @Setter
  private MultiLayerProtocol protocol;

  private final ProtocolService protocolService;

  public MultiLayerProtocolRunner(ProtocolService protocolService) {
    this.protocolService = protocolService;
  }

  public MatcherApi getMatcher() {
    return protocolService.getLinkageUnitService().getMatcherApi();
  }

  public ProtocolService getProtocolService() {
    return protocolService;
  }

  public void fetchProject(int layer, boolean update) {
    fetchProject(protocol.getLayers().get(layer), update);
  }

  public void fetchProject(Layer layer, boolean update) {
    layer.setProject(getMatcher().getProject(layer.getProjectId(), update));
  }

  public void fetchProject(String projectId, boolean update) {
    Layer layer = this.protocol.getLayerOfProject(projectId);
    layer.setProject(getMatcher().getProject(projectId, update));
  }

  private void runNextStep(MultiLayerProtocol protocol) {
    ProcessingStep next = protocol.getStepQueue().removeFirst();
    next = ProcessingStepFactory.createTypedProcessingStep(next);
    long timestamp = System.currentTimeMillis();
    next.execute(this);
    long timestampAfter = System.currentTimeMillis();
    next.getProperties().put("runtime", String.format(
      Locale.ENGLISH, "%.3f", (timestampAfter - timestamp) / 1000.0));
    protocol.getStepHistory().add(next.getAsUntypedStep());
  }

  public MultiLayerProtocol runMultiLayerProtocol(MultiLayerProtocol protocol,
    ProtocolExecutionDto protocolExecution) {
    this.protocol = protocol;
    for (Layer layer : protocol.getLayers()) {
      layer.setProject(
        protocolService.getLinkageUnitService().getMatcherApi().getProject(layer.getProjectId())
      );
    }
    int stepCount = 0;
    while (true) {
      if (!protocol.getStepQueue().isEmpty()) {
        if (protocol.getStepQueue().getFirst().getType().equals(protocolExecution.getStepToStop())
        || (protocolExecution.getNumberOfSteps() > -1 && protocolExecution.getNumberOfSteps() <= stepCount)) {
          break;
        }
        runNextStep(protocol);
        protocolService.save(protocol);
        stepCount++;
      } else {
        MultiLayerActiveLearningWorkflow.addNextSteps(protocol);
        protocolService.save(protocol);
        if (protocol.getStepQueue().isEmpty() ||
          MultiLayerActiveLearningWorkflow.StepType.WAIT_FOR_EXTERNAL_INPUT.toString()
            .equals(protocol.getStepQueue().getFirst().getType())) {
          break;
        }
      }
    }
    return protocol;
  }
}

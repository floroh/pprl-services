package de.unileipzig.dbs.pprl.service.protocol.workflow.steps;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.unileipzig.dbs.pprl.service.common.data.dto.DatasetDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.reporting.ReportGroup;
import de.unileipzig.dbs.pprl.service.linkageunit.data.mongo.PhaseProgress;
import de.unileipzig.dbs.pprl.service.protocol.service.MultiLayerProtocolRunner;
import de.unileipzig.dbs.pprl.service.protocol.service.ProtocolService;
import de.unileipzig.dbs.pprl.service.protocol.workflow.MultiLayerActiveLearningWorkflow;
import de.unileipzig.dbs.pprl.service.protocol.workflow.ProcessingStep;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class TransferEncodedDatasetStep extends ProcessingStep {

  public TransferEncodedDatasetStep() {
    super();
    this.type = getStepType().toString();
  }

  public TransferEncodedDatasetStep(String type, Map<String, String> properties,
    PhaseProgress phaseProgress,
    Map<String, ReportGroup> reportGroups) {
    super(type, properties, phaseProgress, reportGroups);
    this.type = getStepType().toString();
  }

  public static StepType getStepType() {
    return MultiLayerActiveLearningWorkflow.StepType.TRANSFER_ENCODED_DATASET;
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
    int plaintextDatasetId = runner.getProtocol().getPlaintextDatasetId();
    String encodingMethod = runner.getProtocol().getLayers().getFirst().getEncodingMethod();
    int encodedDatasetId = ProtocolService.getEncodedDatasetId(plaintextDatasetId,
      encodingMethod
    );
    List<DatasetDto> datasetDescriptions = runner.getMatcher().getDatasetDescriptions();
    Optional<DatasetDto> first = datasetDescriptions.stream()
      .filter(dto -> dto.getPlaintextDatasetId() == plaintextDatasetId)
      .filter(dto -> dto.getDatasetId() == encodedDatasetId)
      .findFirst();
    if (first.isEmpty()) {
      runner.getProtocolService().addEncodedDataset(plaintextDatasetId, encodingMethod);
    } else {
      getProperties().put("cached", "True");
    }
    getProperties().put("plaintextDatasetId", String.valueOf(plaintextDatasetId));
    getProperties().put("encodedDatasetId", String.valueOf(encodedDatasetId));
    setProjectId(runner.getProtocol().getLayers().getFirst().getProjectId());
    phaseProgress.setProgress(1.0);
    phaseProgress.setDone(true);
  }

  public TransferEncodedDatasetStep setProjectId(String projectId) {
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



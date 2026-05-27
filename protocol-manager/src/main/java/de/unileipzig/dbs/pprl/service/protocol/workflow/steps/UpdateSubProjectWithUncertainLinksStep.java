package de.unileipzig.dbs.pprl.service.protocol.workflow.steps;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.unileipzig.dbs.pprl.service.common.data.dto.*;
import de.unileipzig.dbs.pprl.service.common.data.dto.reporting.ReportGroup;
import de.unileipzig.dbs.pprl.service.dataowner.data.dto.EncodingRetrievalRequestDto;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.MatcherUpdateType;
import de.unileipzig.dbs.pprl.service.linkageunit.data.mongo.PhaseProgress;
import de.unileipzig.dbs.pprl.service.linkageunit.services.LinkImprovementService;
import de.unileipzig.dbs.pprl.service.protocol.model.mongo.Layer;
import de.unileipzig.dbs.pprl.service.protocol.service.MultiLayerProtocolRunner;
import de.unileipzig.dbs.pprl.service.protocol.service.ProtocolService;
import de.unileipzig.dbs.pprl.service.protocol.workflow.MultiLayerActiveLearningWorkflow;
import de.unileipzig.dbs.pprl.service.protocol.workflow.ProcessingStep;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class UpdateSubProjectWithUncertainLinksStep extends ProcessingStep {

  public UpdateSubProjectWithUncertainLinksStep() {
    this.type = getStepType().toString();
  }

  public UpdateSubProjectWithUncertainLinksStep(String type, Map<String, String> properties,
    PhaseProgress phaseProgress,
    Map<String, ReportGroup> reportGroups) {
    super(type, properties, phaseProgress, reportGroups);
    this.type = getStepType().toString();
  }

  public static StepType getStepType() {
    return MultiLayerActiveLearningWorkflow.StepType.UPDATE_SUBPROJECT_WITH_UNCERTAIN_LINKS;
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
    Layer subLayer = runner.getProtocol().getLayerOfProject(getProjectId());
    String parentProjectId =
      subLayer.getProject().getConfigs().get(LinkImprovementService.CONFIG_PROJECT_ID_TO_REPORT_TO);
    int wishLimit = getNumberOfPairsLimit() * 2;
    if (wishLimit == 0) wishLimit = -1;
    List<RecordEncodingWishDto> recordEncodingWishes =
      runner.getMatcher().getRecordEncodingWishes(parentProjectId, wishLimit);

    if (recordEncodingWishes.isEmpty()) {
      log.info("No more record encoding wishes. Aborting...");
//      subLayer.setActive(false);
//      setSuccess(false);
      return;
    } else {
      int usedBudget = subLayer.getNumberOfReviews();
      int numberOfCurrentPairs = recordEncodingWishes.size() / 2;
      if (subLayer.getBudget() > 0) {
        int remainingBudget = subLayer.getBudget() - usedBudget;
        if (remainingBudget < numberOfCurrentPairs) {
          recordEncodingWishes = recordEncodingWishes.subList(0, remainingBudget * 2);
        }
        if (remainingBudget <= 0) {
          subLayer.setActive(false);
          log.debug("Reached clerical review budget.");
        }
      }
      getProperties().put("numberOfPairs", String.valueOf(numberOfCurrentPairs));
      usedBudget += numberOfCurrentPairs;
      subLayer.setNumberOfReviews(usedBudget);
    }

    long plaintextDatasetId = runner.getProtocol().getPlaintextDatasetId();
    List<EncodingRetrievalRequestDto> requests = buildEncodingRequest(
            plaintextDatasetId, recordEncodingWishes, runner.getProtocol().getLinkageProject());
    List<RecordDto> reencodedRecords = getReEncodedRecords(runner.getProtocolService(), requests);
    if (!reencodedRecords.isEmpty()) {
      GroundTruthDto fullGroundTruth =
        runner.getProtocolService().getLinkageUnitService().getMatcherApi()
          .getGroundTruth(runner.getProtocol().getLayerOfProject(parentProjectId).getProject().getDatasetId());

      long encodedDatasetId = subLayer.getProject().getDatasetId();
      runner.getProtocolService().addDescriptionForEncodedDatasetIfMissing(plaintextDatasetId, encodedDatasetId,
              EncodingIdDto.builder().method("UNKNOWN").build());
      addNewRecordsToProject(
        runner.getProtocolService(), encodedDatasetId, fullGroundTruth, reencodedRecords);
      if (MatcherUpdateType.NEW_IMPROVED.equals(subLayer.getUpdateType())) {
        log.debug("Running matcher for new records...");
        subLayer.setProject(runner.getMatcher().runProjectForNewRecords(subLayer.getProjectId()));
      } else {
        runner.getMatcher().fetchPairs(subLayer.getProjectId());
        if (subLayer.isUpdateMatcher()) {
          if (MatcherUpdateType.UPPER_IMPROVED.equals(subLayer.getUpdateType())) {
            log.debug("Updating matcher with upper improved pairs...");
            // update matcher with upper improved pairs
            if (subLayer.getCurrentBatch() < 1) {
              runner.getMatcher().updateMatcher(subLayer.getProjectId(), subLayer.getUpdateType());
            }
          }
        }
      }
    }
//    setSuccess(true);
    runner.fetchProject(subLayer, false);
    subLayer.incrementCurrentBatch();
    phaseProgress.setProgress(1.0);
    phaseProgress.setDone(true);
  }

  private void addNewRecordsToProject(
    ProtocolService protocolService,
    long newDatasetId,
    GroundTruthDto groundTruthFull,
    List<RecordDto> reencodedRecords) {
    reencodedRecords.forEach(record -> record.setDatasetId(newDatasetId));
    log.debug("Inserting reencoded records to matcher...");
    protocolService.getLinkageUnitService().getMatcherApi().batchInsert(reencodedRecords);
    log.debug("Inserted reencoded records to matcher.");

    GroundTruthDto groundTruthPartial = determinePartialGroundTruth(groundTruthFull, reencodedRecords,
      newDatasetId
    );
    try {
      GroundTruthDto previousGroundTruth =
        protocolService.getLinkageUnitService().getMatcherApi().getGroundTruth(newDatasetId);
      Set<String> pairIds = groundTruthPartial.getRecordIdPairs().stream()
        .map(RecordIdPairDto::getUniqueLikePairId)
        .collect(Collectors.toSet());
      previousGroundTruth.getRecordIdPairs().stream()
        .filter(idPair -> !pairIds.contains(idPair.getUniqueLikePairId()))
        .forEach(idPair -> groundTruthPartial.getRecordIdPairs().add(idPair));
    } catch (Exception e) {
      log.debug("No previous ground truth found. Adding new ground truth to matcher...");
    }
    protocolService.getLinkageUnitService().getMatcherApi().addGroundTruth(groundTruthPartial);
    log.debug("Added partial ground truth to matcher.");
  }

  private GroundTruthDto determinePartialGroundTruth(GroundTruthDto groundTruthFull,
    List<RecordDto> reencodedRecords, long newDatasetId) {
    List<String> recordIds = reencodedRecords.stream()
      .map(record -> record.getId().getBlocks().getFirst())
      .toList();
//    log.info("encoded ids:" + recordIds.subList(0, 5));
    List<RecordIdPairDto> remainingPairs = groundTruthFull.getRecordIdPairs().stream()
      .filter(pair -> recordIds.contains(pair.getUniqueLikePairId()))
      .peek(pair -> {
        if (pair.getLeftRecordId().getSource().equals(pair.getRightRecordId().getSource())) {
          log.error("Pair with same source: " + pair);
        }
      })
      .collect(Collectors.toList());
    log.debug("Determined partial ground truth with {} remaining pairs.", remainingPairs.size());
    return GroundTruthDto.builder()
      .datasetId(newDatasetId)
      .recordIdPairs(remainingPairs)
      .build();
  }

  private List<RecordDto> getReEncodedRecords(ProtocolService protocolService,
    List<EncodingRetrievalRequestDto> requests) {
    log.debug("Retrieving {} reencoded records...", requests.size());
    log.debug("First requests: " + requests.getFirst());
    List<RecordDto> reencodedRecords =
      protocolService.getDataOwnerService().getEncoderApi().retrieveMultipleEncoded(requests);
    log.debug("Example reencoded record: " + reencodedRecords.getFirst());

    List<String> encodings = reencodedRecords.stream()
      .map(record -> record.getEncodingId().getMethod())
      .distinct()
      .toList();
    if (encodings.size() > 1) {
      throw new IllegalStateException("Multiple encodings found: " + encodings);
    }
    String method = encodings.getFirst();
    log.debug("Retrieved {} reencoded records with encoding method {} ", reencodedRecords.size(), method);
    return reencodedRecords;
  }

  private static List<EncodingRetrievalRequestDto> buildEncodingRequest(
    long plainDatasetId, List<RecordEncodingWishDto> recordEncodingWishes, String linkageProject) {
    List<EncodingRetrievalRequestDto> requestDtos = recordEncodingWishes.stream()
      .map(wish -> {
          EncodingIdDto curEncodingId = wish.getEncodingId();
          curEncodingId.setProject(linkageProject);
          return EncodingRetrievalRequestDto.builder()
            .encodingId(curEncodingId)
            .datasetId(plainDatasetId)
            .recordSecret(wish.getRecordSecret())
            .recordId(wish.getId())
            .build();
        }
      )
      .collect(Collectors.toList());
    if (requestDtos.isEmpty()) {
      log.error("No encoding wishes found!");
    }
    return requestDtos;
  }

  @JsonIgnore
  public boolean getSuccess() {
    return Boolean.parseBoolean(getProperties().get("success"));
  }

  public UpdateSubProjectWithUncertainLinksStep setSuccess(boolean success) {
    getProperties().put("success", String.valueOf(success));
    return this;
  }

  @JsonIgnore
  public String getProjectId() {
    return getProperties().get("projectId");
  }

  public UpdateSubProjectWithUncertainLinksStep setProjectId(String projectId) {
    getProperties().put("projectId", projectId);
    return this;
  }

  @JsonIgnore
  public int getNumberOfPairsLimit() {
    return Integer.parseInt(getProperties().get("numberOfPairsLimit"));
  }

  public UpdateSubProjectWithUncertainLinksStep setNumberOfPairsLimit(int numberOfPairsLimit) {
    getProperties().put("numberOfPairsLimit", String.valueOf(numberOfPairsLimit));
    return this;
  }

  @Override
  public String toString() {
    return "UpdateSubProjectWithUncertainLinksStep{" +
      "properties=" + properties +
      "} " + super.toString();
  }
}



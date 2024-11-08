package de.unileipzig.dbs.pprl.service.protocol.workflow;

import de.unileipzig.dbs.pprl.service.protocol.model.mongo.Layer;
import de.unileipzig.dbs.pprl.service.protocol.model.mongo.MultiLayerProtocol;
import de.unileipzig.dbs.pprl.service.protocol.workflow.steps.DetermineUncertainLinksStep;
import de.unileipzig.dbs.pprl.service.protocol.workflow.steps.IdleStep;
import de.unileipzig.dbs.pprl.service.protocol.workflow.steps.InitialLinkageStep;
import de.unileipzig.dbs.pprl.service.protocol.workflow.steps.ReclassifyStep;
import de.unileipzig.dbs.pprl.service.protocol.workflow.steps.ReportImprovedPairsStep;
import de.unileipzig.dbs.pprl.service.protocol.workflow.steps.TransferEncodedDatasetStep;
import de.unileipzig.dbs.pprl.service.protocol.workflow.steps.UpdateMatcherStep;
import de.unileipzig.dbs.pprl.service.protocol.workflow.steps.UpdateSubProjectWithUncertainLinksStep;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class MultiLayerActiveLearningWorkflow {

  public enum StepType implements ProcessingStep.StepType {
    TRANSFER_ENCODED_DATASET,
    INITIAL_STATE,
    IDLE,
    WAIT_FOR_EXTERNAL_INPUT,
    RUN_INITIAL_LINKAGE,
    DETERMINE_UNCERTAIN_LINKS,
    GENERATE_WISHES,
    UPDATE_SUBPROJECT_WITH_UNCERTAIN_LINKS,
    RUN_LINKAGE_OF_NEW_RECORDS,
    FETCH_PAIRS,
    UPDATE_MATCHER,
    RECLASSIFY_PAIRS,
    REPORT_PAIRS
  }

  public static MultiLayerProtocol addNextSteps(MultiLayerProtocol protocol) {
    ProcessingStep previousStep;
    if (protocol.getStepHistory().isEmpty()) {
      previousStep = ProcessingStep.builder()
        .type(StepType.INITIAL_STATE.toString())
        .build();
    } else {
      previousStep = protocol.getStepHistory().getLast();
    }
    log.info("Previous step {}", previousStep);
    List<ProcessingStep> nextSteps = new ArrayList<>();
    StepType type = MultiLayerActiveLearningWorkflow.StepType.valueOf(previousStep.getType());
    switch (type) {
      case IDLE -> {
        log.info("IDLE");
      }

      case INITIAL_STATE -> {
        nextSteps.add(TransferEncodedDatasetStep.builder()
          .build()
          .getAsUntypedStep()
        );
      }

      case TRANSFER_ENCODED_DATASET -> {
        nextSteps.add(InitialLinkageStep.builder()
          .build()
          .setProjectId(protocol.getLayers().getFirst().getProjectId())
          .getAsUntypedStep()
        );
      }

      case RUN_INITIAL_LINKAGE -> {
        Layer firstLayer = protocol.getLayers().getFirst();
        Optional<Layer> possibleSubLayer = protocol.getNextLayer(firstLayer);
        if (possibleSubLayer.isPresent()) {
          Layer subLayer = possibleSubLayer.get();
          subLayer.setCurrentBatch(0);
//          subLayer.updateBatchSize();
        }
        nextSteps.add(
          DetermineUncertainLinksStep.builder()
            .build()
            .setProjectId(firstLayer.getProjectId())
            .getAsUntypedStep()
        );
      }

      case DETERMINE_UNCERTAIN_LINKS -> {
        DetermineUncertainLinksStep previousStepTyped =
          (DetermineUncertainLinksStep) ProcessingStepFactory.createTypedProcessingStep(previousStep);
        String projectId = previousStepTyped.getProjectId();
        Layer currentLayer = protocol.getLayerOfProject(projectId);
        Optional<Layer> possibleSubLayer = protocol.getNextLayer(currentLayer);
        Optional<Layer> possibleParentLayer = protocol.getPreviousLayer(currentLayer);
        if (previousStepTyped.getNumberOfUncertainLinks() == 0) {
          if (possibleParentLayer.isPresent()) {
//            currentLayer.setCurrentBatch(0);
            nextSteps.add(ReportImprovedPairsStep.builder()
              .build()
              .setProjectId(currentLayer.getProjectId())
              .getAsUntypedStep()
            );
          } else {
            nextSteps.add(IdleStep.builder()
              .build()
              .setDescription("No uncertain links available")
              .getAsUntypedStep()
            );
          }
        } else if (possibleSubLayer.isPresent()) {
          Layer subLayer = possibleSubLayer.get();
          if (subLayer.reachedBatchLimit() || !subLayer.isActive()) {
            if (!subLayer.isActive()) {
              currentLayer.setUpdateMatcher(false);
            }
            if (possibleParentLayer.isPresent()) {
              subLayer.setCurrentBatch(0);
              nextSteps.add(
                ReportImprovedPairsStep.builder()
                  .build()
                  .setProjectId(currentLayer.getProjectId())
                  .getAsUntypedStep()
              );
            } else {
              nextSteps.add(IdleStep.builder()
                .build()
                .setDescription("Finished")
                .getAsUntypedStep()
              );
            }
          } else {
            nextSteps.add(UpdateSubProjectWithUncertainLinksStep.builder()
              .build()
              .setProjectId(subLayer.getProjectId())
              .setNumberOfPairsLimit(subLayer.getBatchSize())
              .getAsUntypedStep()
            );
          }
        } else {
          nextSteps.add(IdleStep.builder()
                  .build()
                  .setDescription("Finished")
                  .getAsUntypedStep()
          );
        }
      }

      case UPDATE_SUBPROJECT_WITH_UNCERTAIN_LINKS -> {
        UpdateSubProjectWithUncertainLinksStep previousStepTyped =
          (UpdateSubProjectWithUncertainLinksStep) ProcessingStepFactory.createTypedProcessingStep(previousStep);
        String projectId = previousStepTyped.getProjectId();
        nextSteps.add(
          ReclassifyStep.builder()
            .build()
            .setProjectId(projectId)
            .getAsUntypedStep()
        );
      }

      case REPORT_PAIRS -> {
        ReportImprovedPairsStep previousStepTyped =
          (ReportImprovedPairsStep) ProcessingStepFactory.createTypedProcessingStep(previousStep);
        String projectId = previousStepTyped.getProjectId();
        Layer layer = protocol.getLayerOfProject(projectId);
        Layer parentLayer = protocol.getPreviousLayer(layer).orElseThrow();
        if (parentLayer.isUpdateMatcher()) {
          nextSteps.add(
            UpdateMatcherStep.builder()
              .build()
              .setProjectId(parentLayer.getProjectId())
              .getAsUntypedStep()
          );
        } else {
          nextSteps.add(
            DetermineUncertainLinksStep.builder()
              .build()
              .setProjectId(parentLayer.getProjectId())
              .getAsUntypedStep()
          );
        }
      }

      case UPDATE_MATCHER -> {
        UpdateMatcherStep previousStepTyped =
          (UpdateMatcherStep) ProcessingStepFactory.createTypedProcessingStep(previousStep);
        String projectId = previousStepTyped.getProjectId();
        nextSteps.add(
          ReclassifyStep.builder()
            .build()
            .setProjectId(projectId)
            .getAsUntypedStep()
        );
      }

      case RECLASSIFY_PAIRS -> {
        ReclassifyStep previousStepTyped =
          (ReclassifyStep) ProcessingStepFactory.createTypedProcessingStep(previousStep);
        String projectId = previousStepTyped.getProjectId();
        Layer currentLayer = protocol.getLayerOfProject(projectId);
        Optional<Layer> possibleSubLayer = protocol.getNextLayer(currentLayer);
        Optional<Layer> possibleParentLayer = protocol.getPreviousLayer(currentLayer);
        if (possibleSubLayer.isPresent()) {
          Layer subLayer = possibleSubLayer.get();
          if (subLayer.reachedBatchLimit() || !subLayer.isActive()) {
            if (!subLayer.isActive()) {
              currentLayer.setUpdateMatcher(false);
            }
            if (possibleParentLayer.isPresent()) {
              subLayer.setCurrentBatch(0);
              nextSteps.add(
                ReportImprovedPairsStep.builder()
                  .build()
                  .setProjectId(projectId)
                  .getAsUntypedStep()
              );
            } else {
              nextSteps.add(IdleStep.builder()
                .build()
                .setDescription("Finished")
                .getAsUntypedStep()
              );
            }
          } else {
            nextSteps.add(
              DetermineUncertainLinksStep.builder()
                .build()
                .setProjectId(projectId)
                .getAsUntypedStep()
            );
          }
        } else {
          if (possibleParentLayer.isPresent()) {
            nextSteps.add(
              ReportImprovedPairsStep.builder()
                .build()
                .setProjectId(projectId)
                .getAsUntypedStep()
            );
          } else {
            nextSteps.add(
              DetermineUncertainLinksStep.builder()
                .build()
                .setProjectId(currentLayer.getProjectId())
                .getAsUntypedStep()
            );
          }
        }
      }

      default -> {
        nextSteps.add(ProcessingStep.builder()
          .type(StepType.WAIT_FOR_EXTERNAL_INPUT.toString())
          .build()
          .getAsUntypedStep()
        );
      }
    }
    log.info("Next step(s) are: {}", nextSteps);
    protocol.setStepQueue(nextSteps);
    return protocol;
  }

  private static int getWishLimit(Layer layer) {
//    if (layer.getBudget() > 0) {
//      int remainingBudget = layer.getRemainingBudget();
//      int numberOfPairWishes = Math.min(layer.getBatchSize(), remainingBudget);
//      log.info("Checking clerical review budget({}): {} remaining reviews.",
//        layer.getBudget(), remainingBudget
//      );
//      remainingBudget -= numberOfPairWishes;
//      layer.setRemainingBudget(remainingBudget);
//      return numberOfPairWishes * 2;
//    }
    return layer.getBatchSize() * 2;
  }
}

package de.unileipzig.dbs.pprl.service.protocol.workflow;

import de.unileipzig.dbs.pprl.service.protocol.workflow.steps.DetermineUncertainLinksStep;
import de.unileipzig.dbs.pprl.service.protocol.workflow.steps.GeneratingWishesStep;
import de.unileipzig.dbs.pprl.service.protocol.workflow.steps.IdleStep;
import de.unileipzig.dbs.pprl.service.protocol.workflow.steps.InitialLinkageStep;
import de.unileipzig.dbs.pprl.service.protocol.workflow.steps.ReclassifyStep;
import de.unileipzig.dbs.pprl.service.protocol.workflow.steps.ReportImprovedPairsStep;
import de.unileipzig.dbs.pprl.service.protocol.workflow.steps.TransferEncodedDatasetStep;
import de.unileipzig.dbs.pprl.service.protocol.workflow.steps.UpdateMatcherStep;
import de.unileipzig.dbs.pprl.service.protocol.workflow.steps.UpdateSubProjectWithUncertainLinksStep;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class ProcessingStepFactory {
  public static ProcessingStep createTypedProcessingStep(ProcessingStep untypedStep) {
    String type = untypedStep.getType();
    MultiLayerActiveLearningWorkflow.StepType stepType =
      MultiLayerActiveLearningWorkflow.StepType.valueOf(type);
    switch (stepType) {
      case IDLE:
        return IdleStep.builder()
          .properties(untypedStep.properties)
          .phaseProgress(untypedStep.phaseProgress)
          .reportGroups(untypedStep.reportGroups)
          .build();
      case DETERMINE_UNCERTAIN_LINKS:
        return DetermineUncertainLinksStep.builder()
          .properties(untypedStep.properties)
          .phaseProgress(untypedStep.phaseProgress)
          .reportGroups(untypedStep.reportGroups)
          .build();
      case TRANSFER_ENCODED_DATASET:
        return TransferEncodedDatasetStep.builder()
          .properties(untypedStep.properties)
          .phaseProgress(untypedStep.phaseProgress)
          .reportGroups(untypedStep.reportGroups)
          .build();
      case RUN_INITIAL_LINKAGE:
        return InitialLinkageStep.builder()
          .properties(untypedStep.properties)
          .phaseProgress(untypedStep.phaseProgress)
          .reportGroups(untypedStep.reportGroups)
          .build();
      case UPDATE_SUBPROJECT_WITH_UNCERTAIN_LINKS:
        return UpdateSubProjectWithUncertainLinksStep.builder()
          .properties(untypedStep.properties)
          .phaseProgress(untypedStep.phaseProgress)
          .reportGroups(untypedStep.reportGroups)
          .build();
      case RECLASSIFY_PAIRS:
        return ReclassifyStep.builder()
          .properties(untypedStep.properties)
          .phaseProgress(untypedStep.phaseProgress)
          .reportGroups(untypedStep.reportGroups)
          .build();
      case UPDATE_MATCHER:
        return UpdateMatcherStep.builder()
          .properties(untypedStep.properties)
          .phaseProgress(untypedStep.phaseProgress)
          .reportGroups(untypedStep.reportGroups)
          .build();
      case REPORT_PAIRS:
        return ReportImprovedPairsStep.builder()
          .properties(untypedStep.properties)
          .phaseProgress(untypedStep.phaseProgress)
          .reportGroups(untypedStep.reportGroups)
          .build();
      default:
        log.warn("Unknown steptype: " + type);
        return untypedStep;
    }
  }
}

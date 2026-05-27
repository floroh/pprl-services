package de.unileipzig.dbs.pprl.service.protocol.model.mongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.unileipzig.dbs.pprl.service.dataowner.data.dto.EncodingCreationRequestDto;
import de.unileipzig.dbs.pprl.service.linkageunit.config.LinkSelectionStrategy;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.BatchMatchProjectDto;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.MatcherUpdateType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Objects;


@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Slf4j
public class Layer {

  // Config parameters
  private String name;
  private String matcherMethod;
  private List<Integer> batchSizeConfig;
  private int maxBatches;
  private String encodingMethod;
  private boolean updateMatcher;
  private LinkSelectionStrategy linkSelectionStrategy;
  private MatcherUpdateType updateType;

  // RBF
  private double initialThreshold;
  @Builder.Default
  private Boolean stopUpdateWhenClericalReviewBudgetIsReached = false;

  // CR
  @Builder.Default
  private int budget = -1;
  @Builder.Default
  private double errorRate = 0.0;

  // Weights
  private String attributeWeightMethod;
  private Map<String, Double> initialAttributeWeights;
  private Map<String, Double> attributeErrorRates;

  // Runtime parameters
  private String projectId;
  private int batchSize;
  private int currentBatch;

  @Builder.Default
  private int numberOfReviews = 0;

  @Builder.Default
  private boolean active = true;

  @JsonIgnore
  private BatchMatchProjectDto project;

  public void incrementCurrentBatch() {
    currentBatch = currentBatch + 1;
    setCurrentBatch(currentBatch);
  }

  public int getBatchSize() {
    if (batchSize == 0 && batchSizeConfig != null && !batchSizeConfig.isEmpty()) {
      batchSize = batchSizeConfig.getFirst();
    }
    return batchSize;
  }

  public boolean isStopUpdateWhenClericalReviewBudgetIsReached() {
    return Objects.requireNonNullElse(stopUpdateWhenClericalReviewBudgetIsReached, false);
  }

  public void setCurrentBatch(int currentBatch) {
    log.info("Updating current batch of layer {} to {}", name, currentBatch);
    this.currentBatch = currentBatch;
    updateBatchSize();
  }

  public void setNumberOfReviews(int numberOfReviews) {
    this.numberOfReviews = numberOfReviews;
    updateActiveStatus();
  }

  public int updateBatchSize() {
    if (getBatchSizeConfig() == null) {
      return getBatchSize();
    }
    log.debug("BatchSizeConfig (batchnumber=" + getCurrentBatch() + "): " + getBatchSizeConfig());
    int index = Math.min(getCurrentBatch(), getBatchSizeConfig().size() - 1);
    int newBatchSize = getBatchSizeConfig().get(index);
    if (newBatchSize != getBatchSize()) {
      log.info("Updating batch size of layer {} from {} to {}.", getName(), getBatchSize(),
        newBatchSize
      );
      setBatchSize(newBatchSize);
    }
    return getBatchSize();
  }

  public boolean reachedBatchLimit() {
    if (currentBatch >= getMaxBatches()) {
      log.info("Reached batch switch {}>={}. Aborting layer {}.",
        currentBatch, getMaxBatches(), this
      );
      return true;
    }
    return false;
  }

  public void updateActiveStatus() {
    setActive(!skipClericalReviewLayer());
    log.info("Updated active status of layer {} to {}", name, active);
  }

  public boolean skipClericalReviewLayer() {
    return name.contains("CR") & (budget > 0) & (numberOfReviews >= budget);
  }

  public void setProject(BatchMatchProjectDto project) {
    this.project = project;
    this.projectId = project.getProjectId();
  }
}

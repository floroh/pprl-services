package de.unileipzig.dbs.pprl.service.linkageunit.data.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;


/**
 * Request for creating a matching definition.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request for creating a matching definition.")
public class MatchingCreationRequestDto {

  @Builder.Default
  private WeightSelectionMethod weightSelectionMethod = WeightSelectionMethod.FELLEGI_SUNTER;

  @Builder.Default
  private MatchingCreationMethod matchingCreationMethod = MatchingCreationMethod.BASE_CONFIG_ADAPTION;

  private MatcherIdDto baseMatcherId;

  private MatcherIdDto outputMatcherId;

  private Map<String, Double> attributeWeights;

  private Map<String, Double> attributeMWeights;

  private Map<String, Double> attributeUWeights;

  private Long datasetId;

  @Builder.Default
  private boolean persist = true;

  public enum WeightSelectionMethod {
    NONE,
    FELLEGI_SUNTER,
  }

  public enum MatchingCreationMethod {
    BASE_CONFIG_ADAPTION
  }
}

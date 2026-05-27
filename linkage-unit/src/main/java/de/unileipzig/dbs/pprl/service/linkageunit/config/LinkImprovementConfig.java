package de.unileipzig.dbs.pprl.service.linkageunit.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@With
@SuperBuilder(toBuilder = true)
public class LinkImprovementConfig {

  /**
   * The endpoint of the (previous) linkage unit where this linkage unit sends its results to.
   */
  private String linkageUnitEndpointToReportTo;

  /**
   * Select record pairs for improvement alternating between matches and non-matches.
   */
  @Builder.Default
  private LinkSelectionStrategy linkSelectionStrategy = LinkSelectionStrategy.BUCKETS;

  /**
   * If true, additional records pairs of large classes or buckets are not selected for improvement.
   */
  @Builder.Default
  private boolean balancedSelectionOnly = false;

  /**
   * If true, an improved link is only reported once.
   */
  @Builder.Default
  private boolean reportOnlyOnce = true;

  /**
   * The minimum uncertainty of a record pair to be considered for improvement.
   */
  @Builder.Default
  private double minUncertainty = 0.2;

  /**
   * The encoding strategy included in wishes
   */
  @Builder.Default
  private String encodingMethodForWishes = "DBSLeipzig/DUMMY";

  /**
   * The minimal attribute similarity score required to request the plaintext value for it
   */
  @Builder.Default
  private double minSimilarityForPlaintextSelection = 0.4;
}

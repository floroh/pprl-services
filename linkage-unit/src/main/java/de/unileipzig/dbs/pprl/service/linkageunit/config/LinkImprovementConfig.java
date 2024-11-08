package de.unileipzig.dbs.pprl.service.linkageunit.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Data
@AllArgsConstructor
@NoArgsConstructor
@With
public class LinkImprovementConfig {

  /**
   * The endpoint of the (previous) linkage unit where this linkage unit sends its results to.
   */
  private String linkageUnitEndpointToReportTo;

  /**
   * Select record pairs for improvement alternating between matches and non-matches.
   */
  private LinkSelectionStrategy linkSelectionStrategy = LinkSelectionStrategy.BUCKETS;

  /**
   * If true, additional records pairs of large classes or buckets are not selected for improvement.
   */
  private boolean balancedSelectionOnly = false;

  /**
   * If true, an improved link is only reported once.
   */
  private boolean reportOnlyOnce = true;

  /**
   * The minimum uncertainty of a record pair to be considered for improvement.
   */
  private double minUncertainty = 0.2;

  /**
   * The encoding strategy included in wishes
   */
  private String encodingMethodForWishes = "DBSLeipzig/DUMMY";

  /**
   * The minimal attribute similarity score required to request the plaintext value for it
   */
  private double minSimilarityForPlaintextSelection = 0.4;
}

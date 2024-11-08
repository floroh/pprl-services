package de.unileipzig.dbs.pprl.service.linkageunit.data.mongo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PhaseProgress {

  /**
   * True, if this phase is finished
   */
  private boolean done;

  /**
   * Progress in the range 0...1
   */
  private double progress;

  /**
   * Arbitrary textual description of the progress, e.g. "12/561 blocking groups processed"
   */
  private String description;

}

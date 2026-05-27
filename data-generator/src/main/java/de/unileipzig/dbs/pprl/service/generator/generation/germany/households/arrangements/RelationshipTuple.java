package de.unileipzig.dbs.pprl.service.generator.generation.germany.households.arrangements;

import lombok.Getter;
import lombok.Setter;

/**
 * An object of this class describes the federal state, the kind of living arrangement and the age of partner1.
 * With those information, a appropriate partner2 will be calculated.
 */
@Setter
@Getter
public class RelationshipTuple {

  //<editor-fold desc="Standard get and set methods.">
  private String federalState;
  private int typeOfLivingArrangement;
  private int ageOfPartner1;

  private int frequency;

  public RelationshipTuple(String federalState, int typeOfLivingArrangement, int ageOfPartner1) {
    this.federalState = federalState;
    this.typeOfLivingArrangement = typeOfLivingArrangement;
    this.ageOfPartner1 = ageOfPartner1;
  }

  //</editor-fold>
}
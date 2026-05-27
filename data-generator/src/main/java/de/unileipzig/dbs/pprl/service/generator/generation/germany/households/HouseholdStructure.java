package de.unileipzig.dbs.pprl.service.generator.generation.germany.households;

import de.unileipzig.dbs.pprl.service.generator.generation.germany.attributes.person.PersonAttribute;

/**
 * All persons inside a household will have the same HouseholdStructure object.
 * This class is needed to identify the household type easier.
 */
public class HouseholdStructure extends PersonAttribute {

  Household household;

  public HouseholdStructure() {
    /* Evaluation priority not needed. */
    setAttributeName("HOUSEHOLD STRUCTURE");
  }

  //<editor-fold desc="Standard get and set methods.">
  public void setHousehold(Household household) {
    this.household = household;
  }
  //</editor-fold>

  public void determineHouseholdStructure() {
    household.getSelectedLivingArrangement().calculateStructureAndSize();
    setValue_String(
            household.getSelectedLivingArrangement().getLivingArrangementStructure());
  }

  public void determineHouseholdStructure_FlatSharingCommunity() {
    household.getFlatSharingCommunity().calculateFlatSize();
    setValue_String(
            String.valueOf(household.getFlatSharingCommunity().getFlatSize()));
  }
}
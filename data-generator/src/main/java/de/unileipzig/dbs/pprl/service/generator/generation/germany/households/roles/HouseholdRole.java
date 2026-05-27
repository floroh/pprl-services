package de.unileipzig.dbs.pprl.service.generator.generation.germany.households.roles;

import de.unileipzig.dbs.pprl.service.generator.generation.germany.attributes.Attribute;

/**
 * Class used to characterise the role of a person inside a household.
 */
public class HouseholdRole extends Attribute {

  public HouseholdRole() {
    /* Evaluation priority not needed. */
    setAttributeName("HOUSEHOLD ROLE");

  }

  @Override
  public String toString() {
    return getValue_String();
  }
}
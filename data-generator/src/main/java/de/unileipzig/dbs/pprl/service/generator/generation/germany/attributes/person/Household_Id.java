package de.unileipzig.dbs.pprl.service.generator.generation.germany.attributes.person;

/**
 * All persons of a household have the same Household_Id object.
 */
public class Household_Id extends PersonAttribute {

  public Household_Id() {
    /* Evaluation priority not needed. */
    //setEvaluationPriority(1);
    setAttributeName("HH-ID");
  }

  public void nextValue() {
    /* Value for Household_Id is increased in HouseholdGenerator object. */
  }

  @Override
  public void nextValue_personInHousehold() {
    /* Do nothing. */
  }

  public void nextValue_personInFlatSharingCommunity() {
    /* Do nothing. */
  }

  public String getValue_String() {
    return String.valueOf(getValue_int());
  }

  public String toString() {
    return String.valueOf(getValue_int());
  }
}
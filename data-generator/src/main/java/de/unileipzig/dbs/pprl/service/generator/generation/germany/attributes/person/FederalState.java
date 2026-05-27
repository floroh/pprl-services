package de.unileipzig.dbs.pprl.service.generator.generation.germany.attributes.person;

import de.unileipzig.dbs.pprl.service.generator.generation.germany.records.Person;

public class FederalState extends PersonAttribute {

  private CensusTuple censusTuple;

  /* Only needed for household structures. */
  private FederalState referenceFederalState;

  public FederalState() {
    setEvaluationPriority(10);
    setAttributeName("FEDERAL STATE");
  }

  //<editor-fold desc="Standard get and set methods">
  public void setCensusTuple(CensusTuple censusTuple) {
    this.censusTuple = censusTuple;
  }

  public FederalState getReferenceFederalState() {
    return referenceFederalState;
  }

  public void setReferenceFederalState(FederalState referenceFederalState) {
    this.referenceFederalState = referenceFederalState;
  }
  //</editor-fold>

  @Override
  public boolean connectInfluences(Person person) {
    /* If the census tuple was not requested. */
    if (person.getCensusTuple() == null) {
      person.setCensusTuple(new CensusTuple());
      person.getAuxiliaryList().add(person.getCensusTuple());
      this.censusTuple = person.getCensusTuple();
      return true;
    }
    /* The census tuple was requested or is already inside the auxiliary list. */
    else {
      this.censusTuple = person.getCensusTuple();
      return false;
    }
  }

  public void nextValue() {
    setValue_String(this.censusTuple.getFederalState());
  }

  public void nextValue_personInHousehold() {
    setValue_String(this.referenceFederalState.getValue_String());
  }

  public void nextValue_personInFlatSharingCommunity() {
    setValue_String(this.referenceFederalState.getValue_String());
  }
}
package de.unileipzig.dbs.pprl.service.generator.generation.germany.attributes.person;

import de.unileipzig.dbs.pprl.service.generator.generation.germany.records.Person;

public class Gender extends PersonAttribute {

  CensusTuple censusTuple;

  public Gender() {
    setEvaluationPriority(20);
    setAttributeName("GENDER");
  }

  public void setCensusTuple(CensusTuple censusTuple) {
    this.censusTuple = censusTuple;
  }

  @Override
  public boolean connectInfluences(Person person) {
    /* If the census tuple was not requested. */
    if (person.getCensusTuple() == null) {
      person.setCensusTuple(new CensusTuple());
      person.getAuxiliaryList().add(person.getCensusTuple());
      this.censusTuple = person.getCensusTuple();
      return true;
    }
    /* The census tuple was requested and is already inside the auxiliary list. */
    else {
      this.censusTuple = person.getCensusTuple();
      return false;
    }
  }

  @Override
  public void nextValue() {
    setValue_String(censusTuple.getGender());
  }

  @Override
  public void nextValue_personInHousehold() {
    setValue_String(censusTuple.getGender());
  }

  public void nextValue_personInFlatSharingCommunity() {
    setValue_String(censusTuple.getGender());
  }
}
package de.unileipzig.dbs.pprl.service.generator.generation.germany.attributes.person;

import de.unileipzig.dbs.pprl.service.generator.generation.germany.records.Person;

public class Age extends PersonAttribute {

  private CensusTuple censusTuple;

  public Age() {
    setEvaluationPriority(25);
    setAttributeName("AGE");
  }

  public void setCensusTuple(CensusTuple censusTuple) {
    this.censusTuple = censusTuple;
  }

  public String getValue_String() {
    return String.valueOf(getValue_int());
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
    setValue_int(censusTuple.getAge());
  }

  @Override
  public void nextValue_personInHousehold() {
    setValue_int(censusTuple.getAge());
  }

  @Override
  public void nextValue_personInFlatSharingCommunity() {
    setValue_int(censusTuple.getAge());
  }
}
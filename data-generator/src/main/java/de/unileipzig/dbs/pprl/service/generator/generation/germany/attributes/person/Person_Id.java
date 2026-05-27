package de.unileipzig.dbs.pprl.service.generator.generation.germany.attributes.person;

import de.unileipzig.dbs.pprl.service.generator.generation.germany.records.Person;

public class Person_Id extends PersonAttribute {

  public Person_Id() {
    setEvaluationPriority(0);
    setAttributeName("PERSON-ID");

    setValue_int(0);
  }

  @Override
  public boolean connectInfluences(Person person) {
    return false;
  }

  @Override
  public void nextValue() {
    setValue_int(getValue_int() + 1);
  }

  @Override
  public void nextValue_personInHousehold() {
    setValue_int(getValue_int() + 1);
  }

  public void nextValue_personInFlatSharingCommunity() {
    setValue_int(getValue_int() + 1);
  }

  @Override
  public String getValue_String() {
    return String.valueOf(getValue_int());
  }
}
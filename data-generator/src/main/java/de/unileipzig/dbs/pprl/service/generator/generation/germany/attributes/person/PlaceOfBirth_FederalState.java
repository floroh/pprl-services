package de.unileipzig.dbs.pprl.service.generator.generation.germany.attributes.person;

import de.unileipzig.dbs.pprl.service.generator.generation.germany.builders.RandomGeneratorBuilder;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.randomgenerator.RandomGenerator;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.records.Person;

import java.util.Map;

public class PlaceOfBirth_FederalState extends PersonAttribute {

  DateOfBirth dateOfBirth;

  Map<Object, RandomGenerator> placeOfBirth_federalStatesGenerator;

  public PlaceOfBirth_FederalState() {
    setEvaluationPriority(28);
    setAttributeName("PLACE OF BIRTH - FEDERAL STATE");
    placeOfBirth_federalStatesGenerator = RandomGeneratorBuilder.buildRandomGenerators(this, null);
  }

  public void setDateOfBirth(DateOfBirth dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
  }

  @Override
  public boolean connectInfluences(Person person) {
    if (person.getDateOfBirth() == null) {
      person.setDateOfBirth(new DateOfBirth());
      person.getAuxiliaryList().add(person.getDateOfBirth());
      this.dateOfBirth = person.getDateOfBirth();
      return true;
    } else {
      this.dateOfBirth = person.getDateOfBirth();
      return false;
    }
  }

  @Override
  public void nextValue() {
    if (dateOfBirth.getDateOfBirth().getYear() <= 1990) {
      setValue_String(placeOfBirth_federalStatesGenerator.get("1990").next_String());
    } else if (dateOfBirth.getDateOfBirth().getYear() >= 2020) {
      setValue_String(placeOfBirth_federalStatesGenerator.get("2020").next_String());
    } else {
      setValue_String(placeOfBirth_federalStatesGenerator
              .get(String.valueOf(dateOfBirth.getDateOfBirth().getYear()))
              .next_String());
    }
  }

  @Override
  public void nextValue_personInHousehold() {
    nextValue();
  }

  public void nextValue_personInFlatSharingCommunity() {
    nextValue();
  }
}
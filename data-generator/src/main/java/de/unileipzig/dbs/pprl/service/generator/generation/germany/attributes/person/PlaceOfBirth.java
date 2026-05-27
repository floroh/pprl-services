package de.unileipzig.dbs.pprl.service.generator.generation.germany.attributes.person;

import de.unileipzig.dbs.pprl.service.generator.generation.germany.builders.RandomGeneratorBuilder;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.randomgenerator.RandomGenerator;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.records.Person;

import java.util.Map;

public class PlaceOfBirth extends PersonAttribute {

  PlaceOfBirth_FederalState placeOfBirth_federalState;

  Map<Object, RandomGenerator> placeOfBirthGenerators;

  public PlaceOfBirth() {
    setEvaluationPriority(30);
    setAttributeName("PLACE OF BIRTH");
    placeOfBirthGenerators = RandomGeneratorBuilder.buildRandomGenerators(this, null);
  }

  public void setPlaceOfBirth_federalState(PlaceOfBirth_FederalState placeOfBirth_federalState) {
    this.placeOfBirth_federalState = placeOfBirth_federalState;
  }

  @Override
  public boolean connectInfluences(Person person) {
    if (person.getPlaceOfBirth_FederalState() == null) {
      person.setPlaceOfBirth_FederalState(new PlaceOfBirth_FederalState());
      person.getAuxiliaryList().add(person.getPlaceOfBirth_FederalState());
      this.placeOfBirth_federalState = person.getPlaceOfBirth_FederalState();
      return true;
    } else {
      this.placeOfBirth_federalState = person.getPlaceOfBirth_FederalState();
      return false;
    }
  }

  @Override
  public void nextValue() {
    if (placeOfBirthGenerators.containsKey(placeOfBirth_federalState.getValue_String())) {
      setValue_String(placeOfBirthGenerators.get(placeOfBirth_federalState.getValue_String()).next_String());
    } else {
      setValue_String("");
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
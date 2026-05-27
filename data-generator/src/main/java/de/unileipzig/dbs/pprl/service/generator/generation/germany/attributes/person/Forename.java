package de.unileipzig.dbs.pprl.service.generator.generation.germany.attributes.person;

import de.unileipzig.dbs.pprl.service.generator.generation.germany.builders.RandomGeneratorBuilder;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.randomgenerator.RandomGenerator;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.records.Person;

import java.util.Map;

public class Forename extends PersonAttribute {

  private Gender gender;
  private DateOfBirth dateOfBirth;

  private final Map<Object, RandomGenerator> femaleNameGenerators;
  private final Map<Object, RandomGenerator> maleNameGenerators;

  public Forename() {
    setEvaluationPriority(40);
    setAttributeName("FORENAME");
    femaleNameGenerators = RandomGeneratorBuilder.buildRandomGenerators(this, "female");
    maleNameGenerators = RandomGeneratorBuilder.buildRandomGenerators(this, "male");
  }

  //<editor-fold desc="Standard get and set methods.">
  public void setGender(Gender gender) {
    this.gender = gender;
  }

  public void setDateOfBirth(DateOfBirth dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
  }
  //</editor-fold>

  public int getDecadeFromDateOfBirth() {
    if (dateOfBirth.getDateOfBirth().getYear() < 1910) {
      return 1900;
    } else if (dateOfBirth.getDateOfBirth().getYear() >= 2010) {
      return 2010;
    } else {
      return (dateOfBirth.getDateOfBirth().getYear() / 10) * 10;
    }
  }

  @Override
  public boolean connectInfluences(Person person) {
    if (person.getGender() == null) {
      person.setGender(new Gender());
      person.getAuxiliaryList().add(person.getGender());
      this.gender = person.getGender();
      return true;
    } else if (person.getDateOfBirth() == null) {
      person.setDateOfBirth(new DateOfBirth());
      person.getAuxiliaryList().add(person.getDateOfBirth());
      this.dateOfBirth = person.getDateOfBirth();
      return true;
    } else {
      this.gender = person.getGender();
      this.dateOfBirth = person.getDateOfBirth();
      return false;
    }
  }

  @Override
  public void nextValue() {

    if (gender.getValue_String().equalsIgnoreCase("F")) {
      setValue_String(femaleNameGenerators.get(getDecadeFromDateOfBirth()).next_String());

    } else if (gender.getValue_String().equalsIgnoreCase("M")) {
      setValue_String(maleNameGenerators.get(getDecadeFromDateOfBirth()).next_String());

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
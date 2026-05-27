package de.unileipzig.dbs.pprl.service.generator.generation.germany.attributes.person;

import de.unileipzig.dbs.pprl.service.generator.generation.germany.builders.RandomGeneratorBuilder;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.randomgenerator.RandomGenerator;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.records.Person;

public class Surname extends PersonAttribute {

  private final RandomGenerator surnameGenerator;

  public Surname() {
    setEvaluationPriority(80);
    setAttributeName("SURNAME");
    this.surnameGenerator = RandomGeneratorBuilder.buildRandomGenerator(this);
  }

  @Override
  public boolean connectInfluences(Person person) {
    return false;
  }

  @Override
  public void nextValue() {
    setValue_String(surnameGenerator.next_String());
  }

  @Override
  public void nextValue_personInHousehold() {
    /* Calculated by SurnameConfigurator separately. */
  }

  public void nextValue_personInFlatSharingCommunity() {
    nextValue();
  }
}
package de.unileipzig.dbs.pprl.service.generator.generation.germany.attributes.person;

import de.unileipzig.dbs.pprl.service.generator.generation.germany.builders.RandomGeneratorBuilder;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.randomgenerator.RandomGenerator;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.records.Person;

import java.util.Map;

public class ZipCode extends PersonAttribute {

  private FederalState federalState;

  /* Only needed for household structures. */
  private ZipCode referenceZipCode;

  private final Map<Object, RandomGenerator> zipCodeGenerators;

  public ZipCode() {
    setEvaluationPriority(50);
    setAttributeName("ZIP CODE");
    this.zipCodeGenerators = RandomGeneratorBuilder.buildRandomGenerators(this, null);
  }

  //<editor-fold desc="Standard get and set methods">
  public void setFederalState(FederalState federalState) {
    this.federalState = federalState;
  }

  public void setReferenceZipCode(ZipCode referenceZipCode) {
    this.referenceZipCode = referenceZipCode;
  }
  //</editor-fold>

  @Override
  public boolean connectInfluences(Person person) {
    if (person.getFederalState() == null) {
      person.setFederalState(new FederalState());
      person.getAuxiliaryList().add(person.getFederalState());
      this.federalState = person.getFederalState();
      return true;
    } else {
      this.federalState = person.getFederalState();
      return false;
    }
  }

  @Override
  public void nextValue() {
    setValue_String(zipCodeGenerators.get(federalState.getValue_String()).next_String());
  }

  public void nextValue_personInHousehold() {
    setValue_String(referenceZipCode.getValue_String());
  }

  public void nextValue_personInFlatSharingCommunity() {
    setValue_String(referenceZipCode.getValue_String());
  }
}
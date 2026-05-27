package de.unileipzig.dbs.pprl.service.generator.generation.germany.attributes.person;

import de.unileipzig.dbs.pprl.service.generator.generation.germany.builders.RandomGeneratorBuilder;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.randomgenerator.RandomGenerator;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.records.Person;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Street extends PersonAttribute {

  private ZipCode zipCode;

  /* Only needed for the generation of households. */
  private Street referenceStreet;

  private final Map<Object, RandomGenerator> streetGenerators;

  // For testing: Collection containing zip codes without streets
  List<String> zipCodesWithoutStreets = new ArrayList<>();

  public Street() {
    setEvaluationPriority(70);
    setAttributeName("STREET");
    this.streetGenerators = RandomGeneratorBuilder.buildRandomGenerators(this, null);
  }

  //<editor-fold desc="Standard get and set methods.">
  public List<String> getZipCodesWithoutStreets() {
    return zipCodesWithoutStreets;
  }

  public void setZipCode(ZipCode zipCode) {
    this.zipCode = zipCode;
  }

  public Street getReferenceStreet() {
    return referenceStreet;
  }

  public void setReferenceStreet(Street referenceStreet) {
    this.referenceStreet = referenceStreet;
  }
  //</editor-fold>

  @Override
  public boolean connectInfluences(Person person) {
    if (person.getZipCode() == null) {
      person.setZipCode(new ZipCode());
      person.getAuxiliaryList().add(person.getZipCode());
      this.zipCode = person.getZipCode();
      return true;
    } else {
      this.zipCode = person.getZipCode();
      return false;
    }
  }

  @Override
  public void nextValue() {
    if (streetGenerators.containsKey(zipCode.getValue_String())) {
      setValue_String(streetGenerators.get(zipCode.getValue_String()).next_String());
    } else {
      setValue_String("");

      /* For testing: Collection contains zip codes without streets. */
      if (!zipCodesWithoutStreets.contains(zipCode.getValue_String())) {
        zipCodesWithoutStreets.add(zipCode.getValue_String());
      }
    }
  }

  @Override
  public void nextValue_personInHousehold() {
    setValue_String(referenceStreet.getValue_String());
  }

  public void nextValue_personInFlatSharingCommunity() {
    setValue_String(referenceStreet.getValue_String());
  }
}
package de.unileipzig.dbs.pprl.service.generator.generation.germany.attributes.person;

import de.unileipzig.dbs.pprl.service.generator.generation.germany.builders.RandomGeneratorBuilder;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.randomgenerator.RandomGenerator;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.records.Person;

import java.util.*;

public class Location extends PersonAttribute {

  ZipCode zipCode;

  /* Only needed during the generation of households. */
  private Location referenceLocation;

  /* One zip code can belong to multiple locations
   * (small villages distributed over a larger area often use one zip code).
   * One location can have multiple zip codes (like big cities).
   * To simplify the implementation, a map is used for this relationship. The keys are the zip codes
   * and the values are RandomGenerator objects. If one location has many zip codes, there are multiple keys
   * inside the map, all linked to a RandomGenerator object with only one possible solution.
   * If one zip code has multiple locations, the RandomGenerator object uses a uniform probability distribution for
   * all those locations.
   */
  private final Map<Object, RandomGenerator> locationGenerators;

  public Location() {
    setEvaluationPriority(60);
    setAttributeName("LOCATION");
    this.locationGenerators = RandomGeneratorBuilder.buildRandomGenerators(this, null);
  }

  //<editor-fold desc="Standard get and set methods.">
  public Location getReferenceLocation() {
    return referenceLocation;
  }

  public void setReferenceLocation(Location referenceLocation) {
    this.referenceLocation = referenceLocation;
  }

  public void setZipCode(ZipCode zipCode) {
    this.zipCode = zipCode;
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

  public void nextValue() {
    if (locationGenerators.containsKey(zipCode.getValue_String())) {
      setValue_String(locationGenerators.get(zipCode.getValue_String()).next_String());
    } else {
      setValue_String("");
    }
  }

  @Override
  public void nextValue_personInHousehold() {
    setValue_String(referenceLocation.getValue_String());
  }

  public void nextValue_personInFlatSharingCommunity() {
    setValue_String(referenceLocation.getValue_String());
  }
}
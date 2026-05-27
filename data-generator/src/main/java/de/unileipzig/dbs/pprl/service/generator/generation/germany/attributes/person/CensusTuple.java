package de.unileipzig.dbs.pprl.service.generator.generation.germany.attributes.person;

import de.unileipzig.dbs.pprl.service.generator.generation.germany.builders.*;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.randomgenerator.RandomGenerator;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.records.Person;

import java.util.Objects;

public class CensusTuple extends PersonAttribute implements Comparable<CensusTuple> {

  private RandomGenerator censusTupleGenerator;

  String[] censusArray;
  private String federalState;
  private String gender;
  private int age;

  /* Frequency only needed, if object is inside CensusTuplePool. */
  private int frequency;

  public CensusTuple() {
    setEvaluationPriority(5);
    setAttributeName("CENSUS TUPLE");
    this.censusTupleGenerator = RandomGeneratorBuilder.buildRandomGenerator(this);
  }

  /* Constructor for objects inside CensusTuplePool. */
  public CensusTuple(String federalState, String gender, int age, int frequency) {
    this.federalState = federalState;
    this.gender = gender;
    this.age = age;
    this.frequency = frequency;
  }

  //<editor-fold desc="Standard get and set methods">
  public String getFederalState() {
    return federalState;
  }

  public void setFederalState(String federalState) {
    this.federalState = federalState;
  }

  public String getGender() {
    return gender;
  }

  public void setGender(String gender) {
    this.gender = gender;
  }

  public int getAge() {
    return age;
  }

  public void setAge(int age) {
    this.age = age;
  }

  public int getFrequency() {
    return frequency;
  }

  public void setFrequency(int frequency) {
    this.frequency = frequency;
  }
  //</editor-fold>

  @Override
  public boolean connectInfluences(Person record) {
    return false;
  }

  public void nextValue() {
    setValue_String(censusTupleGenerator.next_String());
    censusArray = getValue_String().split(",");
    federalState = censusArray[0];
    gender = censusArray[1];
    age = Integer.parseInt(censusArray[2]);
  }

  public void nextValue_personInHousehold() {
    /* Do, nothing. */
  }

  public void nextValue_personInFlatSharingCommunity() {
    /* Do nothing. */
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CensusTuple that = (CensusTuple) o;
    return federalState.equals(that.federalState) &&
            gender.equals(that.gender) &&
            age == that.age;
  }

  @Override
  public int hashCode() {
    return Objects.hash(federalState, gender, age);
  }

  @Override
  public String toString() {
    return "federalState='" + federalState + '\'' + ", gender='" + gender + '\'' + ", age='" + age + '\'' +
            ", frequency=" + frequency;
  }

  /**
   * This method enables the sorting of Census tuple objects inside a collection ascending according to the value for
   * age.
   *
   * @param o CensusTuple object, that is compared with this.
   * @return Value is 0, with this and o have same age, negative if this has the lower value, positive if this has the
   * bigger value.
   */
  @Override
  public int compareTo(CensusTuple o) {
    return Integer.compare(this.age, o.age);
  }

  public void reduceFrequencyBy(int i) {
    frequency -= i;
  }

  public void increaseFrequencyBy(int i) {
    frequency += i;
  }
}
package de.unileipzig.dbs.pprl.service.generator.generation.germany.attributes;

import de.unileipzig.dbs.pprl.service.generator.generation.germany.records.Person;

public abstract class Attribute {

  private int evaluationPriority;
  private String attributeName;

  private String value_String;
  private int value_int;

  //<editor-fold desc="Standard get and set methods">
  public int getEvaluationPriority() {
    return evaluationPriority;
  }

  public void setEvaluationPriority(int evaluationPriority) {
    this.evaluationPriority = evaluationPriority;
  }

  public String getAttributeName() {
    return attributeName;
  }

  public void setAttributeName(String attributeName) {
    this.attributeName = attributeName;
  }

  public String getValue_String() {
    return value_String;
  }

  public void setValue_String(String value_String) {
    this.value_String = value_String;
  }

  public int getValue_int() {
    return value_int;
  }

  public void setValue_int(int value) {
    this.value_int = value;
  }
  //</editor-fold>

  public void nextValue() {
  }

  /**
   * Method is used, when household structures are requested. Used during phase 1 & 2 instead of the nextValue()
   * method. Will avoid the generation of separate address and family names for all household members in phase 1 & 2.
   */
  public void nextValue_personInHousehold() {
  }

  /**
   * Method is used, when household structures are requested. Used during phase 1 & 2 instead of the nextValue()
   * method. Will avoid the generation of separate address for all household members in phase 1 & 2.
   */
  public void nextValue_personInFlatSharingCommunity() {
  }

  /**
   * This method will be overridden by concrete attribute objects.
   *
   * @param person Instance of Person class to connect it's attributes dependencies.
   * @return Only true, if a attribute object is added to the auxiliary list.
   */
  public boolean connectInfluences(Person person) {
    return false;
  }
}
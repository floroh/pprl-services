package de.unileipzig.dbs.pprl.service.generator.generation.germany.records;

import de.unileipzig.dbs.pprl.service.generator.generation.germany.attributes.Attribute;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public abstract class Record {

  private final StringBuilder sb;

  /**
   * Contains all the requested Attributes.Attribute objects in the order they were requested.
   */
  @Setter
  @Getter
  private List<Attribute> requestedAttributes;
  /**
   * Contains the same requested Attributes.Attribute objects (references) like in the attributes List.
   * Additionally Attributes.Attribute objects are added, in case they are need for evaluation but were not requested.
   * This List will be sorted by the configureDependencies() method.
   */
  @Setter
  @Getter
  private List<Attribute> auxiliaryList;

  public Record() {
    sb = new StringBuilder();
    requestedAttributes = new ArrayList<>();
    auxiliaryList = new ArrayList<>();
  }

  public void nextValues() {
    for (Attribute a : auxiliaryList) {
      a.nextValue();
    }
  }

  /**
   * This method returns the names of all requested Attributes.Attribute objects.
   *
   * @return A String which contains the names of all requested Attributes.Attribute objects, separated with delimiter ",".
   */
  public String attributeNamesToString() {
    sb.setLength(0);
    for (Attribute a : requestedAttributes) {
      sb.append(a.getAttributeName()).append(";");
    }
    sb.deleteCharAt(sb.length() - 1);
    return sb.toString();
  }

  /**
   * This method returns the values of all requested Attributes.Attribute objects at that particular moment.
   *
   * @return A String which contains the values of all requested Attributes.Attribute objects at that particular moment, separated with delimiter ",".
   */
  public String attributeValuesToString() {
    sb.setLength(0);
    for (Attribute a : requestedAttributes) {
      sb.append(a.getValue_String()).append(";");
    }
    sb.deleteCharAt(sb.length() - 1);
    return sb.toString();
  }

  public abstract void configureDependencies();
}
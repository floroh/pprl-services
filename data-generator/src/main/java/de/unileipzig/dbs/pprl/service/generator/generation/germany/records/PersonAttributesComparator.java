package de.unileipzig.dbs.pprl.service.generator.generation.germany.records;

import de.unileipzig.dbs.pprl.service.generator.generation.germany.attributes.Attribute;

import java.util.Comparator;

public class PersonAttributesComparator implements Comparator<Attribute> {

  /**
   * This method compares two Attributes.Attribute Objects according to their evaluationPriority.
   */
  @Override
  public int compare(Attribute a1, Attribute a2) {

    return a1.getEvaluationPriority() - a2.getEvaluationPriority();
  }
}
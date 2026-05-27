package de.unileipzig.dbs.pprl.core.common.selector;

import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;

import java.util.Optional;

/**
 * Select records that have a certain (non-empty) attribute
 */
public class AttributePresent implements Selector<Record> {

  private String attributeName;

  public AttributePresent(String attributeName) {
    this.attributeName = attributeName;
  }

  private AttributePresent() {
  }

  @Override
  public boolean test(Record record) {
    Optional<Attribute> optionalAttribute = record.getAttribute(attributeName);
    return optionalAttribute.filter(attribute -> !attribute.isEmpty()).isPresent();
  }

  public String getAttributeName() {
    return attributeName;
  }

  public void setAttributeName(String attributeName) {
    this.attributeName = attributeName;
  }
}

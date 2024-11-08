package de.unileipzig.dbs.pprl.core.analyzer.cluster.data;

import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;

public class AttributePair extends Pair<Attribute> {
  protected String attributeName;

  public AttributePair(String attributeName, Attribute attr0, Attribute attr1) {
    super(attr0, attr1);
    this.attributeName = attributeName;
  }

  public String getAttributeName() {
    return attributeName;
  }

  @Override
  public String toString() {
    return "AttributePair{" + "v0=" + v0 + ", v1=" + v1 + ", attributeName='" + attributeName + '\'' + '}';
  }
}

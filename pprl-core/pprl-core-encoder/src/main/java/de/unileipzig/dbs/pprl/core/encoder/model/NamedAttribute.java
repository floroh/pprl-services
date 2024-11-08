package de.unileipzig.dbs.pprl.core.encoder.model;

import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;

public class NamedAttribute implements Attribute {

  private String id;

  private Attribute attribute;

  public NamedAttribute(String id, Attribute attribute) {
    this.id = id;
    this.attribute = attribute;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Attribute getAttribute() {
    return attribute;
  }

  public void setAttribute(Attribute attribute) {
    this.attribute = attribute;
  }

  @Override
  public Type getType() {
    return attribute.getType();
  }

  @Override
  public Object getObject() {
    return attribute.getObject();
  }

  @Override
  public Attribute duplicate() {
    return new NamedAttribute(id, attribute.duplicate());
  }
}

package de.unileipzig.dbs.pprl.service.dataowner.modifier.attribute;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.unileipzig.dbs.pprl.core.common.selector.SelectAll;
import de.unileipzig.dbs.pprl.core.common.selector.Selector;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property = "@class")
public class SelectiveAttributeModifier<T> {

  private AttributeModifier<T> modifier;

  private Selector<String> selector;

  public SelectiveAttributeModifier(AttributeModifier<T> modifier) {
    this(modifier, new SelectAll<>());
  }

  public SelectiveAttributeModifier(
    AttributeModifier<T> modifier, Selector<String> selector) {
    this.modifier = modifier;
    this.selector = selector;
  }

  private SelectiveAttributeModifier() {
  }

  public AttributeModifier<T> getModifier() {
    return modifier;
  }

  public void setModifier(AttributeModifier<T> modifier) {
    this.modifier = modifier;
  }

  public Selector<String> getSelector() {
    return selector;
  }

  public void setSelector(Selector<String> selector) {
    this.selector = selector;
  }
}

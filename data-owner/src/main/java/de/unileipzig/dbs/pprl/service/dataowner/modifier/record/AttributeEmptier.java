package de.unileipzig.dbs.pprl.service.dataowner.modifier.record;

import de.unileipzig.dbs.pprl.core.common.factories.AttributeFactory;
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;

import java.util.List;
import java.util.Optional;

public class AttributeEmptier implements RecordModifier {
  public static final String TAG_POSTFIX = "_EMPTIED";
  public static final String DEFAULT_EMPTY_VALUE_PLACEHOLDER = "";

  private String attributeName;

  private String emptyValuePlaceholder = DEFAULT_EMPTY_VALUE_PLACEHOLDER;

  public AttributeEmptier(String attributeName, String emptyValuePlaceholder) {
    this.attributeName = attributeName;
    this.emptyValuePlaceholder = emptyValuePlaceholder;
  }

  public AttributeEmptier(String attributeName) {
    this.attributeName = attributeName;
  }

  private AttributeEmptier() {
  }

  @Override
  public Record modify(Record in) {
    Record out = in.duplicate();
    if (out.getAttribute(attributeName).isEmpty()) {
      return out;
    }

    // Clear value
    Optional<Attribute> optAttr = in.getAttribute(attributeName);
    out.setAttribute(attributeName, emptyAttribute());
    return out;
  }

  @Override
  public List<String> getTags() {
    return List.of(attributeName + TAG_POSTFIX);
  }

  private Attribute emptyAttribute() {
    return AttributeFactory.getAttribute(emptyValuePlaceholder);
  }

  public String getAttributeName() {
    return attributeName;
  }

  public void setAttributeName(String attributeName) {
    this.attributeName = attributeName;
  }

}

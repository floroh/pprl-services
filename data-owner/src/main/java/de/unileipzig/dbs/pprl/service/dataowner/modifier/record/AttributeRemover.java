package de.unileipzig.dbs.pprl.service.dataowner.modifier.record;

import de.unileipzig.dbs.pprl.core.common.model.api.Record;

import java.util.List;

public class AttributeRemover implements RecordModifier {
  public static final String TAG_POSTFIX = "_REMOVED";

  private String attributeName;

  public AttributeRemover(String attributeName) {
    this.attributeName = attributeName;
  }

  private AttributeRemover() {
  }

  @Override
  public Record modify(Record in) {
    Record out = in.duplicate();
    if (out.getAttribute(attributeName).isEmpty()) {
      return out;
    }

    out.removeAttribute(attributeName);
    return out;
  }

  @Override
  public List<String> getTags() {
    return List.of(attributeName + TAG_POSTFIX);
  }

  public String getAttributeName() {
    return attributeName;
  }

  public void setAttributeName(String attributeName) {
    this.attributeName = attributeName;
  }

}

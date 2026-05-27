package de.unileipzig.dbs.pprl.service.dataowner.modifier.record;

import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;

import java.util.List;

public class AttributeSwapper implements RecordModifier {
  public static final String TAG_POSTFIX = "_SWAPPED";

  private String attributeName0;

  private String attributeName1;

  public AttributeSwapper(String attributeName0, String attributeName1) {
    this.attributeName0 = attributeName0;
    this.attributeName1 = attributeName1;
  }

  private AttributeSwapper() {
  }

  @Override
  public Record modify(Record in) {
    Attribute attr0 = in.getAttribute(attributeName0).orElseThrow(() -> new RuntimeException("Missing " +
      "attribute: " + attributeName0));
    Attribute attr1 = in.getAttribute(attributeName1).orElseThrow(() -> new RuntimeException("Missing " +
      "attribute: " + attributeName1));
    Record out = in.duplicate();
    out.setAttribute(attributeName0, attr1);
    out.setAttribute(attributeName1, attr0);
    return out;
  }

  @Override
  public List<String> getTags() {
    return List.of(attributeName0 + "_" + attributeName1 + TAG_POSTFIX);
  }

  public String getAttributeName0() {
    return attributeName0;
  }

  public void setAttributeName0(String attributeName0) {
    this.attributeName0 = attributeName0;
  }

  public String getAttributeName1() {
    return attributeName1;
  }

  public void setAttributeName1(String attributeName1) {
    this.attributeName1 = attributeName1;
  }

}

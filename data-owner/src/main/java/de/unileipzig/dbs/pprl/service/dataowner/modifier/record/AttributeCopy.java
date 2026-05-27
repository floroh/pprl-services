package de.unileipzig.dbs.pprl.service.dataowner.modifier.record;

import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;

import java.util.List;

public class AttributeCopy implements RecordModifier {
  public static final String TAG_POSTFIX = "_COPY_SAME";
  private String originalAttributeName;
  private String additionalAttributeName;

  public AttributeCopy(String originalAttributeName, String additionalAttributeName) {
    this.originalAttributeName = originalAttributeName;
    this.additionalAttributeName = additionalAttributeName;
  }

  private AttributeCopy() {
  }

  @Override
  public Record modify(Record in) {
    Attribute attr = in.getAttribute(originalAttributeName).orElseThrow(() -> new RuntimeException("Missing " +
      "attribute: " + originalAttributeName));
    Record out = in.duplicate();
    out.setAttribute(additionalAttributeName, attr.duplicate());
    return out;
  }

  @Override
  public List<String> getTags() {
    return List.of(originalAttributeName + "_" + additionalAttributeName + TAG_POSTFIX);
  }

  public String getOriginalAttributeName() {
    return originalAttributeName;
  }

  public void setOriginalAttributeName(String originalAttributeName) {
    this.originalAttributeName = originalAttributeName;
  }

  public String getAdditionalAttributeName() {
    return additionalAttributeName;
  }

  public void setAdditionalAttributeName(String additionalAttributeName) {
    this.additionalAttributeName = additionalAttributeName;
  }

}

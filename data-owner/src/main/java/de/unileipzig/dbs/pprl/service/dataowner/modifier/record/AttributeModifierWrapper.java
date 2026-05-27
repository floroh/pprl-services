package de.unileipzig.dbs.pprl.service.dataowner.modifier.record;

import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.service.dataowner.modifier.attribute.AttributeModifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AttributeModifierWrapper implements RecordModifier {

  Map<String, List<AttributeModifier>> attributeModifiers;

  public AttributeModifierWrapper(
    Map<String, List<AttributeModifier>> attributeModifiers) {
    this.attributeModifiers = attributeModifiers;
  }

  public AttributeModifierWrapper(String attributeName, List<AttributeModifier> modifiers) {
    this(Map.of(attributeName, modifiers));
  }

  public AttributeModifierWrapper(String attributeName, AttributeModifier modifier) {
    this(attributeName, List.of(modifier));
  }

  @Override
  public Record modify(Record record) {
    for (Map.Entry<String, List<AttributeModifier>> curAttributeModifiers :
      attributeModifiers.entrySet()) {
      String attributeName = curAttributeModifiers.getKey();
      for (AttributeModifier attributeModifier : curAttributeModifiers.getValue()) {
        record.setAttribute(
          attributeName,
          attributeModifier.modifyToAttribute(
            record.getAttribute(attributeName).get().getAsString()
          )
        );
      }
    }
    return record;
  }

  @Override
  public List<String> getTags() {
    List<String> tags = new ArrayList<>();
    for (Map.Entry<String, List<AttributeModifier>> attributeModifier : attributeModifiers.entrySet()) {
      for (AttributeModifier curAttributeModifier : attributeModifier.getValue()) {
        tags.add(attributeModifier.getKey() + curAttributeModifier.getTagPostFix());
      }
    }
    return tags;
  }
}

package de.unileipzig.dbs.pprl.service.common.data.converter;

import de.unileipzig.dbs.pprl.core.common.factories.AttributeFactory;
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.serialization.AttributeSerializationType;
import de.unileipzig.dbs.pprl.core.common.serialization.SerializationUtils;
import de.unileipzig.dbs.pprl.service.common.data.dto.AttributeDto;

public class AttributeConverter {

  public static Attribute fromDto(AttributeDto dto) {
    AttributeSerializationType attrType = AttributeSerializationType.valueOf(dto.getType());
    return AttributeFactory.parseAttribute(attrType, dto.getValue());
  }

  public static AttributeDto toDto(Attribute attribute) {
    AttributeSerializationType attributeSerializationType =
      SerializationUtils.typeToSerializationType(attribute.getType());
    return AttributeDto.builder()
      .type(attributeSerializationType.name())
      .value(AttributeFactory.attributeToString(attributeSerializationType, attribute))
      .build();
  }
}

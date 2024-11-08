package de.unileipzig.dbs.pprl.core.common.serialization;

import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;

public class SerializationUtils {

  public static Attribute.Type serializationTypeToType(AttributeSerializationType attributeSerializationType) {
    switch (attributeSerializationType) {
      case NULL:
        return Attribute.Type.NULL;
      case STRING:
        return Attribute.Type.STRING;
      case BITSET_BITSTRING:
      case BITSET_BASE64:
        return Attribute.Type.BITVECTOR;
      case INT:
        return Attribute.Type.INT;
    }
    throw new RuntimeException("Invalid attribute type: " + attributeSerializationType);
  }

  public static AttributeSerializationType typeToSerializationType(Attribute.Type type) {
    switch (type) {
      case NULL:
        return AttributeSerializationType.NULL;
      case STRING:
        return AttributeSerializationType.STRING;
      case BITVECTOR:
        return AttributeSerializationType.BITSET_BASE64;
      case INT:
        return AttributeSerializationType.INT;
    }
    throw new RuntimeException("Invalid attribute type: " + type);
  }
}

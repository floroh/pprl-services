package de.unileipzig.dbs.pprl.core.common.serialization;

import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;

public class SerializationUtils {

  public static Attribute.Type serializationTypeToType(AttributeSerializationType attributeSerializationType) {
    return switch (attributeSerializationType) {
      case NULL -> Attribute.Type.NULL;
      case STRING -> Attribute.Type.STRING;
      case BITSET_BITSTRING, BITSET_BASE64 -> Attribute.Type.BITVECTOR;
      case INT -> Attribute.Type.INT;
    };
  }

  public static AttributeSerializationType typeToSerializationType(Attribute.Type type) {
    return switch (type) {
      case NULL -> AttributeSerializationType.NULL;
      case STRING -> AttributeSerializationType.STRING;
      case BITVECTOR -> AttributeSerializationType.BITSET_BASE64;
      case INT -> AttributeSerializationType.INT;
    };
  }
}

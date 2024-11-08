/*
 * Copyright © 2018 - 2021 Leipzig University (Database Research Group)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.unileipzig.dbs.pprl.core.common.factories;

import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.serialization.AttributeSerializationType;
import de.unileipzig.dbs.pprl.core.common.model.api.BitVector;
import de.unileipzig.dbs.pprl.core.common.model.api.ListAttribute;
import de.unileipzig.dbs.pprl.core.common.model.impl.AttributeLight;
import de.unileipzig.dbs.pprl.core.common.model.impl.BitSetVector;
import de.unileipzig.dbs.pprl.core.common.model.impl.ListAttributeLight;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AttributeFactory {
  public enum AttributeVariant {LIGHT}

  public static final String NULL_STRING_REPRESENTATION = ""; // NaN?

  public static Attribute getAttribute(Object obj) {
    return getAttribute(AttributeVariant.LIGHT, obj);
  }

  public static Attribute getAttribute(AttributeVariant variant, Object obj) {
    switch (variant) {
      default:
      case LIGHT:
        if (obj == null) {
          return AttributeLight.NULL_ATTRIBUTE;
        }
        if (List.class.isAssignableFrom(obj.getClass())) {
          @SuppressWarnings("unchecked") Attribute attr = new ListAttributeLight((List<Object>) obj);
          return attr;
        }
        return new AttributeLight(obj);
    }
  }

  public static Attribute parseAttribute(AttributeSerializationType type, String attributeValue) {
    switch (type) {
      case NULL:
        return AttributeLight.NULL_ATTRIBUTE;
      case STRING:
        return parseAttribute(attributeValue, Function.identity());
      case BITSET_BITSTRING:
        return parseAttribute(attributeValue, BitSetVector::fromBitString);
      case BITSET_BASE64:
        return parseAttribute(attributeValue, BitSetVector::fromBase64);
      case INT:
        return parseAttribute(attributeValue, Integer::parseInt);
    }
    return getAttribute(attributeValue);
  }

  private static <T> Attribute parseAttribute(String string, Function<String, T> objParser) {
    boolean isList = string.startsWith("[") && string.endsWith("]");
    if (isList) {
      String[] parts = StringUtils.substringsBetween(string, "[", "]");
      List<T> objects = Arrays.stream(parts)
        .map(objParser)
        .collect(Collectors.toList());
      return getAttribute(objects);
    } else {
      T object = objParser.apply(string);
      return getAttribute(object);
    }
  }

  public static String attributeToString(AttributeSerializationType type, Attribute attribute) {
    switch (type) {
      case NULL:
        return NULL_STRING_REPRESENTATION;
      case STRING:
        return attributeToString(attribute, String.class, Function.identity());
      case BITSET_BITSTRING:
        return attributeToString(attribute, BitVector.class, BitVector::getBitString);
      case BITSET_BASE64:
        return attributeToString(attribute, BitVector.class, BitVector::getBase64);
      case INT:
        return attributeToString(attribute, Integer.class, Object::toString);
    }
    return attribute.getAsString();
  }

  private static <T> String attributeToString(Attribute attribute, Class<T> type,
    Function<T, String> objParser) {
    if (attribute instanceof ListAttribute) {
      List<T> list = ((ListAttribute) attribute).getListAs(type);
      return list.stream()
        .map(objParser)
        .map(s -> "[" + s + "]")
        .collect(Collectors.joining(""));
    } else {
      return objParser.apply(attribute.getAs(type));
    }
  }
}

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

package de.unileipzig.dbs.pprl.core.common.model.api;

import java.io.Serializable;

import static de.unileipzig.dbs.pprl.core.common.factories.AttributeFactory.NULL_STRING_REPRESENTATION;

/**
 * Wraps an attribute value of a {@link Record}
 */
public interface Attribute extends Serializable {

  enum Type {
    NULL,
    STRING,
    BITVECTOR,
    INT
  }

  Type getType();

  Object getObject();

  Attribute duplicate();

  default <T> T getAs(Class<T> c) {
    if (!isType(c)) {
      throw new RuntimeException("Cannot convert attribute of type " + getTypeString() + " as " +
        (c == null ? "null" : c.getSimpleName()));
    }
    @SuppressWarnings("unchecked") T ret = (T) getObject();
    return ret;
  }

  default boolean isType(Class c) {
    switch (getType()) {
      case NULL:
        return c == null;
      case STRING:
        return c.isAssignableFrom(String.class);
      case BITVECTOR:
        return c.isAssignableFrom(BitVector.class);
      case INT:
        return c.isAssignableFrom(Integer.class);
    }
    return false;
  }

  default boolean isNull() {
    return getType().equals(Type.NULL);
  }

  default boolean isString() {
    return isType(String.class);
  }

  default boolean isEmpty() {
    if (isNull()) {
      return true;
    }
    if (getType().equals(Type.BITVECTOR) && ((BitVector)getObject()).getCardinality() == 0) {
      return true;
    }
    if (isString() && getAsString().isEmpty()) {
      return true;
    }
    return false;
  }

  default String getTypeString() {
    return getType().name();
  }

  default String getAsString() {
    if (isString()) {
      return getAs(String.class);
    } else if (isType(BitVector.class)) {
      return (getAs(BitVector.class)).getBitString();
    } else if (isNull()) {
      return NULL_STRING_REPRESENTATION;
    } else {
      return getObject().toString();
    }
  }

}

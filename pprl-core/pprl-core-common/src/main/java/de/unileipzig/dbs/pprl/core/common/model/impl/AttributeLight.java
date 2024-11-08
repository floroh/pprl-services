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

package de.unileipzig.dbs.pprl.core.common.model.impl;

import de.unileipzig.dbs.pprl.core.common.ByteUtils;
import de.unileipzig.dbs.pprl.core.common.model.api.BitVector;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Wraps an attribute value of a {@link Record}
 * It is immutable and therefore cacheable
 */
public class AttributeLight implements Attribute {
  public static final Attribute NULL_ATTRIBUTE = new AttributeLight(null);

  public static final byte OFFSET = 0x01;

  public static final byte TYPE_NULL = 0x00;

  public static final byte TYPE_STRING = 0x01;

  public static final byte TYPE_BITVECTOR = 0x02;

  public static final byte TYPE_INTEGER = 0x03;

  public static final byte TYPE_HASH = 0x04;

  public AttributeLight(Object value) {
    setObject(value);
  }

  protected AttributeLight() {
  }

  /**
   * Stores the type and the value
   */
  protected byte[] rawBytes;

  @Override
  public boolean isNull() {
    return rawBytes[0] == TYPE_NULL;
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean isType(Class c) {
    if (c == null) {
      return isNull();
    }
    if (isNull()) {
      return false;
    }
    try {
      return c.isAssignableFrom(getTypeClass());
    } catch (Exception e) {
      throw new RuntimeException(e.fillInStackTrace());
    }
  }

  protected byte getTypeByte(Object value) {
    if (value == null) {
      return TYPE_NULL;
    } else if (value instanceof String) {
      return TYPE_STRING;
    } else if (value instanceof Integer) {
      return TYPE_INTEGER;
    } else if (value instanceof BitVector) {
      return TYPE_BITVECTOR;
    } else if (value instanceof HashValue) {
      return TYPE_HASH;
    } else {
      throw new RuntimeException("Unsupported attribute type: " + value.getClass()
        .getSimpleName());
    }
  }

  protected Class getTypeClass() {
    switch (rawBytes[0]) {
      case TYPE_NULL:
        return null;
      case TYPE_STRING:
        return String.class;
      case TYPE_INTEGER:
        return Integer.class;
      case TYPE_BITVECTOR:
        return BitVector.class;
      case TYPE_HASH:
        return HashValue.class;
      default:
        throw new RuntimeException("Invalid internal attribute type: " + rawBytes[0]);
    }
  }

  @Override
  public Type getType() {
    switch (rawBytes[0]) {
      case TYPE_NULL:
        return Type.NULL;
      case TYPE_STRING:
        return Type.STRING;
      case TYPE_INTEGER:
        return Type.INT;
      case TYPE_BITVECTOR:
        return Type.BITVECTOR;
      default:
        throw new RuntimeException("Invalid internal attribute type: " + rawBytes[0]);
    }
  }

  public boolean isString() {
    return rawBytes[0] == TYPE_STRING;
  }

  public boolean isInt() {
    return rawBytes[0] == TYPE_INTEGER;
  }

  public boolean isBitVector() {
    return rawBytes[0] == TYPE_BITVECTOR;
  }

  public boolean isHash() {
    return rawBytes[0] == TYPE_HASH;
  }

  public Object getObject() {
    if (isString()) {
      return getString();
    } else if (isInt()) {
      return getInt();
    } else if (isBitVector()) {
      return getBitVector();
    } else if (isHash()) {
      return getHash();
    } else {
      return null;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AttributeLight that = (AttributeLight) o;
    return Arrays.equals(rawBytes, that.rawBytes);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(rawBytes);
  }

  @Override
  public Attribute duplicate() {
    return new AttributeLight(getObject());
  }

  private void setObject(Object value) {
    if (value == null) {
      rawBytes = new byte[] {TYPE_NULL};
    } else if (value instanceof String) {
      setString((String) value);
    } else if (value instanceof Integer) {
      setInt((Integer) value);
    } else if (value instanceof BitVector) {
      setBitSet((BitVector) value);
    } else if (value instanceof HashValue) {
      setHash((HashValue) value);
    } else {
      throw new RuntimeException("Unsupported attribute type: " + value.getClass().getCanonicalName());
    }
  }

  private void setString(String value) {
    byte[] valueBytes = value.getBytes(StandardCharsets.UTF_8);
    rawBytes = new byte[valueBytes.length + OFFSET];
    rawBytes[0] = TYPE_STRING;
    System.arraycopy(valueBytes, 0, rawBytes, OFFSET, valueBytes.length);
  }

  private void setInt(int value) {
    byte[] valueBytes = ByteUtils.intToByteArray(value);
    rawBytes = new byte[valueBytes.length + OFFSET];
    rawBytes[0] = TYPE_INTEGER;
    System.arraycopy(valueBytes, 0, rawBytes, OFFSET, valueBytes.length);
  }

  private void setBitSet(BitVector bv) {
    byte[] valueBytes = bv.toByteArray();
    rawBytes = new byte[valueBytes.length + OFFSET];
    rawBytes[0] = TYPE_BITVECTOR;
    System.arraycopy(valueBytes, 0, rawBytes, OFFSET, valueBytes.length);
  }

  private void setHash(HashValue value) {
    byte[] valueBytes = value.getHash();
    rawBytes = new byte[valueBytes.length + OFFSET];
    rawBytes[0] = TYPE_HASH;
    System.arraycopy(valueBytes, 0, rawBytes, OFFSET, valueBytes.length);
  }

  private String getString() {
    return new String(rawBytes, OFFSET, rawBytes.length - OFFSET, StandardCharsets.UTF_8);
  }

  private int getInt() {
    return ByteUtils.intFromByteArray(getValueBytes());
  }

  private BitVector getBitVector() {
    return BitSetVector.fromByteArray(getValueBytes());
  }

  private HashValue getHash() {
    return new HashValue(getValueBytes());
  }

  protected byte[] getValueBytes() {
    return Arrays.copyOfRange(rawBytes, OFFSET, rawBytes.length);
  }

  @Override
  public String toString() {
    return "AttributeLight{" + "type=" + getTypeString() + ", " + "value=" +
      (isNull() ? "null" : getObject().toString()) + '}';
  }
}

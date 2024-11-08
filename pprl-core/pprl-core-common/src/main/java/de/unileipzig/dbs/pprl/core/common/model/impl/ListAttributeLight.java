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

import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.ListAttribute;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.List;

/**
 * Wraps multiple attribute values of the same type of a {@link Record}
 * It is immutable and therefore cacheable.
 */
public class ListAttributeLight extends AttributeLight implements ListAttribute {
  public static final Attribute NULL_ATTRIBUTE = new ListAttributeLight(Collections.emptyList());

  public ListAttributeLight(List values) {
    setList(values);
  }

  @Override
  public Object getObject() {
    return getList();
  }

  @Override
  public <T> T getAs(Class<T> c) {
    List<T> list = getListAs(c);
    return list.getFirst();
  }

  @Override
  public <T> List<T> getListAs(Class<T> c) {
    if (!isType(c)) {
      throw new RuntimeException("Cannot convert attribute of type " + getTypeString() + " as " +
        (c == null ? "null" : c.getSimpleName()));
    }
    @SuppressWarnings("unchecked") List<T> list = getList();
    return list;
  }

  @Override
  public Attribute duplicate() {
    return new ListAttributeLight(getList());
  }

  private void setList(List list) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try {
      ObjectOutputStream oos = new ObjectOutputStream(bos);
      oos.writeObject(list);
    } catch (IOException e) {
      throw new RuntimeException("Failed to serialize list attribute");
    }
    byte[] valueBytes = bos.toByteArray();

    rawBytes = new byte[valueBytes.length + OFFSET];
    rawBytes[0] = (list == null || list.isEmpty()) ? TYPE_NULL : getTypeByte(list.getFirst());
    System.arraycopy(valueBytes, 0, rawBytes, OFFSET, valueBytes.length);
  }

  private List getList() {
    ByteArrayInputStream bis = new ByteArrayInputStream(getValueBytes());
    try {
      ObjectInputStream ois = new ObjectInputStream(bis);
      List list = (List) ois.readObject();
      return list;
    } catch (Exception e) {
      throw new RuntimeException("Failed to deserialize list attribute");
    }
  }

  @Override
  public String toString() {
    return "ListAttributeLight{" + "type=" + getTypeString() + ", " + "value=" +
      (isNull() ? "null" : getObject().toString()) + '}';
  }
}

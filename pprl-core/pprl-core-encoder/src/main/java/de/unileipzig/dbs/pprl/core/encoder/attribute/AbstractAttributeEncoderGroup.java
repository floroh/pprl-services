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

package de.unileipzig.dbs.pprl.core.encoder.attribute;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.ListAttribute;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.encoder.crypto.KeyExtractor;
import de.unileipzig.dbs.pprl.core.encoder.crypto.KeyedEncoderComponent;
import de.unileipzig.dbs.pprl.core.encoder.hardening.Hardener;
import de.unileipzig.dbs.pprl.core.encoder.hardening.KeyedHardener;
import de.unileipzig.dbs.pprl.core.common.preprocessing.RecordPreprocessor;
import de.unileipzig.dbs.pprl.core.encoder.model.NamedAttribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class AbstractAttributeEncoderGroup<T> implements AttributeEncoderGroup<T>,
  KeyedEncoderComponent {
  protected String id;
  protected RecordPreprocessor recordPreprocessor;
  protected List<Hardener<T>> hardeners;
  protected List<KeyedHardener> keyedHardeners;

  @JsonIgnore
  protected String key;

  public AbstractAttributeEncoderGroup(String id) {
    this();
    this.id = id;
  }

  protected AbstractAttributeEncoderGroup() {
    this.hardeners = new ArrayList<>();
    this.keyedHardeners = new ArrayList<>();
  }

  @Override
  public NamedAttribute encodeToSingleAttribute(Record record) {
    return new NamedAttribute(id, encodeToAttribute(record));
  }

  public abstract Attribute encodeToAttribute(Record record);

  @Override
  public void setKey(String key) {
    this.key = key;
  }

  protected Record preprocess(Record record) {
    if (recordPreprocessor == null) {
      return record;
    }
    return recordPreprocessor.preprocess(record);
  }

  protected T encodeAttribute(AttributeEncoder<?, T> encoder, Attribute attribute) {
    if (encoder instanceof KeyedEncoderComponent) {
      ((KeyedEncoderComponent) encoder).setKey(key);
    }
    return encoder.encode(attribute);
  }

  protected List<T> encodeListAttribute(AttributeEncoder<?, T> encoder, ListAttribute attribute) {
    if (encoder instanceof KeyedEncoderComponent) {
      ((KeyedEncoderComponent) encoder).setKey(key);
    }
    return encoder.encode(attribute);
  }

  protected T harden(T value) {
    for (Hardener<T> bvh : hardeners) {
      value = bvh.harden(value);
    }
    return value;
  }

  protected List<T> hardenList(List<T> list) {
    return list.stream()
      .map(this::harden)
      .collect(Collectors.toList());
  }

  protected <K> T keyedHarden(T value, Record record) {
    for (KeyedHardener<T> bvh : keyedHardeners) {
      Optional<String> optionalKey = KeyExtractor.extractKey(record);
      if (optionalKey.isPresent()) {
        value = bvh.harden(value, optionalKey.get());
      } else {
        throw new RuntimeException("Keyed hardener requires key to be present in record as an attribute " +
          "named \"" + KeyExtractor.KEY_ATTRIBUTE_NAME + "\".");
      }
    }
    return value;
  }

  protected List<T> keyedHardenList(List<T> list, Record record) {
    return list.stream()
      .map(value -> keyedHarden(value, record))
      .collect(Collectors.toList());
  }

  @Override
  public AbstractAttributeEncoderGroup<T> addRecordPreprocessor(RecordPreprocessor recordPreprocessor) {
    this.recordPreprocessor = recordPreprocessor;
    return this;
  }

  @Override
  public AbstractAttributeEncoderGroup<T> addHardener(Hardener<T> hardener) {
    hardeners.add(hardener);
    return this;
  }

  public AbstractAttributeEncoderGroup<T>  addKeyedHardener(KeyedHardener<T> keyedHardener) {
    keyedHardeners.add(keyedHardener);
    return this;
  }

  public String getId() {
    return id;
  }

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public RecordPreprocessor getRecordPreprocessor() {
    return recordPreprocessor;
  }

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public List<Hardener<T>> getHardeners() {
    return hardeners;
  }

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public List<KeyedHardener> getKeyedHardeners() {
    return keyedHardeners;
  }

  private String buildBitVectorId(String attribute, String aeId) {
    return attribute + "_" + aeId;
  }
}

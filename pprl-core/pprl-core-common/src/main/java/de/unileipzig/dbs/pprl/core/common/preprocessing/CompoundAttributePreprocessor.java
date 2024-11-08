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

package de.unileipzig.dbs.pprl.core.common.preprocessing;

import java.util.ArrayList;
import java.util.List;

public class CompoundAttributePreprocessor implements AttributePreprocessor<Object, Object> {

  private List<AttributePreprocessor<Object, Object>> preprocessors;

  public CompoundAttributePreprocessor(AttributePreprocessor<Object, Object> p) {
    preprocessors = new ArrayList<>();
    preprocessors.add(p);
  }

  private CompoundAttributePreprocessor() {
    preprocessors = new ArrayList<>();
  }

  @Override
  public Object preprocess(Object value) {
    Object tmp = value;
    for (AttributePreprocessor<Object, Object> preprocessor : preprocessors) {
      assert (preprocessor.getInputClass()
        .isAssignableFrom(tmp.getClass()));
      tmp = preprocessor.preprocess(tmp);
    }
    return tmp;
  }

  @Override
  public Class<Object> getInputClass() {
    return preprocessors.getFirst()
      .getInputClass();
  }

  @Override
  public Class<Object> getOutputClass() {
    return preprocessors.getLast()
      .getOutputClass();
  }

  public CompoundAttributePreprocessor addPreprocessor(AttributePreprocessor<Object, Object> p) {
    preprocessors.add(p);
    return this;
  }

  public List<AttributePreprocessor<Object, Object>> getPreprocessors() {
    return preprocessors;
  }
}

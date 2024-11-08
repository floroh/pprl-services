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

import de.unileipzig.dbs.pprl.core.common.model.api.CountVector;

import java.util.Arrays;

public class IntVector implements CountVector<Integer> {
  private int[] values;

  public IntVector(int length) {
    values = new int[length];
    Arrays.fill(values, 0);
  }

  @Override
  public void set(int pos, Integer value) {
    values[pos] = value;
  }

  @Override
  public Integer get(int pos) {
    return values[pos];
  }

  @Override
  public void inc(int pos) {
    set(pos, get(pos) + 1);
  }

  @Override
  public int getLength() {
    return values.length;
  }

  @Override
  public long getSum() {
    long sum = 0L;
    for (int value : values) {
      sum += value;
    }
    return sum;
  }

  public double[] getAsDoubleArray() {
    double[] out = new double[getLength()];
    for (int i = 0; i < getLength(); i++) {
      out[i] = values[i];
    }
    return out;
  }
}

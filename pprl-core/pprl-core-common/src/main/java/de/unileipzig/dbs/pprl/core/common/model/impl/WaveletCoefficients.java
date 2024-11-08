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

import de.unileipzig.dbs.pprl.core.common.model.api.NumericArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WaveletCoefficients implements NumericArray<Double> {
  private Double[] coeffs;

  public WaveletCoefficients(double[] coeffs) {
    this.coeffs = Arrays.stream(coeffs)
      .boxed()
      .toArray(Double[]::new);
  }

  @Override
  public Double getElement(int pos) {
    return coeffs[pos];
  }

  @Override
  public List<Double> getElements() {
    return new ArrayList<Double>(Arrays.asList(coeffs));
  }

  @Override
  public String toString() {
    return "WaveletCoefficients{" + "coeffs=" + Arrays.toString(coeffs) + '}';
  }
}

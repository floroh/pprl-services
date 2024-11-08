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

public interface CountVector<T> {

  /**
   * Set the value at a position in the vector
   *
   * @param pos   position to be set
   * @param value value to set
   */
  void set(int pos, T value);

  /**
   * Get the value of a position in the vector
   *
   * @param pos position
   */
  T get(int pos);

  /**
   * Increment the value at a position in the vector
   *
   * @param pos position to increment
   */
  void inc(int pos);

  /**
   * Get the length of the count vector
   *
   * @return length
   */
  int getLength();

  /**
   * Get the sum of all position counts
   *
   * @return sum
   */
  long getSum();
}

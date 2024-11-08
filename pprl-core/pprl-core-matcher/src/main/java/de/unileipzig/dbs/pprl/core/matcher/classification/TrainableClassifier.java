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

package de.unileipzig.dbs.pprl.core.matcher.classification;


import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;

import java.util.Collection;

/**
 * A {@link Classifier} that can be trained and updated.
 */
public interface TrainableClassifier extends Classifier {

  /**
   * Train the classifier based on the given labeled record pairs.
   */
  void fit(Collection<RecordPair> recordPairs);

  void update(RecordPair newRecordPair);

  /**
   * Update the classifier based on the new labeled record pairs.
   */
  default void update(Collection<RecordPair> newRecordPairs) {
    newRecordPairs.forEach(this::update);
  }
}

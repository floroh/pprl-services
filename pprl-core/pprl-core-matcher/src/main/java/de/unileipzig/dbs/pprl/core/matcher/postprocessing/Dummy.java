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

package de.unileipzig.dbs.pprl.core.matcher.postprocessing;

import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;

import java.util.Collection;

public class Dummy implements LinksPostprocessor {

  @Override
  public <P extends RecordPair> Collection<P> clean(Collection<P> recordPairs) {
    return recordPairs;
  }
}

/*
 * Copyright © 2018 - 2020 Leipzig University (Database Research Group)
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

package de.unileipzig.dbs.pprl.core.analyzer.tags.attributepair;

import de.unileipzig.dbs.pprl.core.common.FormattingUtils;
import de.unileipzig.dbs.pprl.core.common.model.api.BitVector;
import de.unileipzig.dbs.pprl.core.common.monitoring.Tag;

import java.util.Collections;
import java.util.List;

public class BitVectorCardinalityDiffAnalyzer implements AttributePairAnalyzer<BitVector> {

  public static final String CARDDIFF = "CARDDIFF";

  @Override
  public List<Tag> getTags(String attrName, BitVector attr0, BitVector attr1) {
    int lendiff = Math.abs(attr0.getCardinality() - attr1.getCardinality());
    return Collections.singletonList(Tag.create(attrName, CARDDIFF,
      FormattingUtils.roundToString(lendiff, 5),
      (double)lendiff
    ));
  }
}

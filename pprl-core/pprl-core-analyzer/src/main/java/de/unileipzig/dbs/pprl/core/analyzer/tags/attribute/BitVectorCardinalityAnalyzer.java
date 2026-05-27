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

package de.unileipzig.dbs.pprl.core.analyzer.tags.attribute;

import de.unileipzig.dbs.pprl.core.common.FormattingUtils;
import de.unileipzig.dbs.pprl.core.common.model.api.BitVector;
import de.unileipzig.dbs.pprl.core.common.monitoring.Tag;

import java.util.ArrayList;
import java.util.List;

public class BitVectorCardinalityAnalyzer implements AttributeAnalyzer<BitVector> {

  public static final String CARD = "CARDINALITY";
  public static final String FILLRATE = "FILLRATE";

  @Override
  public List<Tag> getTags(String attrName, BitVector attr) {
    long card = attr.getCardinality();
    double fillrate = (double)card / attr.getLength();
    List<Tag> tags = new ArrayList<>();
    tags.add(Tag.create(attrName, CARD,
      FormattingUtils.roundToString((int)(card), 5),
      FormattingUtils.roundToDouble(card, 1))
    );
    tags.add(Tag.create(attrName, FILLRATE,
      FormattingUtils.roundToString((int)(fillrate * 100), 5),
      FormattingUtils.roundToDouble(fillrate, 3))
    );
    return tags;
  }

}

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

import de.unileipzig.dbs.pprl.core.common.monitoring.Tag;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.ArrayList;
import java.util.List;

public class StringDistanceAttributePairAnalyzer implements AttributePairAnalyzer<String> {

  public static final String LEV_DISTANCE = "LEV_DISTANCE";
  public static final String MINORDIST = "MINORDIST";
  public static final String MAJORDIST = "MAJORDIST";

  @Override
  public List<Tag> getTags(String attrName, String attr0, String attr1) {
    int dist = LevenshteinDistance.getDefaultInstance().apply(attr0, attr1);
    List<Tag> tags = new ArrayList<>();
        tags.add(Tag.create(attrName, LEV_DISTANCE, String.valueOf(dist), (double) dist));

    double relDist = (double) dist / mean(attr0.length(), attr1.length());
      if (relDist < 0.2) {
          tags.add(Tag.create(attrName, MINORDIST,null, null));
      }
//        if (relDist >= 0.2 && relDist < 0.8) tags.add(attrName + "_MIDDLEDIST");
      if (relDist >= 0.8) {
        tags.add(Tag.create(attrName, MAJORDIST,null, null));
      }
    return tags;
  }

  private double mean(int i0, int i1) {
    return (i0 + i1) / 2.0;
  }
}

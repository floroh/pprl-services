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

package de.unileipzig.dbs.pprl.core.analyzer.tags.recordpair;

import de.unileipzig.dbs.pprl.core.analyzer.tags.attribute.BitVectorCardinalityAnalyzer;
import de.unileipzig.dbs.pprl.core.analyzer.tags.attributepair.BitVectorCardinalityDiffAnalyzer;
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.BitVector;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import de.unileipzig.dbs.pprl.core.common.monitoring.Tag;
import de.unileipzig.dbs.pprl.core.matcher.TagUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BitVectorRecordPairAnalyzer implements RecordPairAnalyzer {

  public static final String TAG_PREFIX = "ENC";
  public static final String ALL_EQUAL = TAG_PREFIX + "_ALL_EQUAL";
  public static final String ALL_WITH_DIFFS = TAG_PREFIX + "_ALL_WITH_DIFFS";
  public static final String ATTR_DIFFCOUNT = TAG_PREFIX + "_ATTR_DIFFCOUNT_";
  public static final String MISSING = "_" + TAG_PREFIX + "_MISSING";
  public static final String MISSING_BOTH = MISSING + "_BOTH";
  public static final String MISSING_ONE = MISSING + "_ONE";
  public static final String EQUAL = TAG_PREFIX + "_EQUAL";
  public static final String UNEQUAL = TAG_PREFIX + "_UNEQUAL";
  public static final String SUBSET = "_" + TAG_PREFIX + "_SUBSET";
  public static final String FILL_RATE = "_" + TAG_PREFIX + "_FILL_RATE";

  BitVectorCardinalityAnalyzer card = new BitVectorCardinalityAnalyzer();
  BitVectorCardinalityDiffAnalyzer cardDiff = new BitVectorCardinalityDiffAnalyzer();

  @Override
  public List<Tag> getTags(RecordPair recordPair) {
    List<String> attributeNames = getAttributeNames(recordPair);
    Record leftRecord = recordPair.getLeftRecord();
    Record rightRecord = recordPair.getRightRecord();

    List<Tag> tags = new ArrayList<>();
    int diffCount = 0;
    for (String attrName : attributeNames) {
      Optional<Attribute> optAttr0 = leftRecord.getAttribute(attrName);
      Optional<Attribute> optAttr1 = rightRecord.getAttribute(attrName);
      if (isEmpty(optAttr0) && isEmpty(optAttr1)) {
        tags.add(TagUtils.create(recordPair, attrName, MISSING_BOTH));
        diffCount++;
        continue;
      }
      if (isEmpty(optAttr0) || isEmpty(optAttr1)) {
        tags.add(TagUtils.create(recordPair, attrName, MISSING_ONE));
        diffCount++;
        continue;
      }

      Attribute attr0 = optAttr0.get();
      Attribute attr1 = optAttr1.get();
      if (!(attr0.getType().equals(Attribute.Type.BITVECTOR) && attr1.getType().equals(Attribute.Type.BITVECTOR))) {
        return tags;
      }
      BitVector bv0 = attr0.getAs(BitVector.class);
      BitVector bv1 = attr1.getAs(BitVector.class);

      // Cardinality
      card.getTags(attrName, bv0).stream()
          .map(t -> Tag.addID(t, leftRecord))
            .forEach(tags::add);
      card.getTags(attrName, bv1).stream()
          .map(t -> Tag.addID(t, rightRecord))
            .forEach(tags::add);

      if (bv0.equals(bv1)) {
        tags.add(TagUtils.create(recordPair, attrName, EQUAL));
        continue;
      }
      tags.add(TagUtils.create(recordPair, attrName, UNEQUAL));
      diffCount++;

      // Cardinality Diff
      tags.addAll(cardDiff.getTags(attrName, bv0, bv1));

      // TODO Subset
//      List<BitVector> sortedByCard = Arrays.asList(bv0, bv1);
//      sortedByCard.sort(Comparator.comparingInt(BitVector::getCardinality));
//
//      if (sortedByCard.get(1).getBitSet().(sortedByCard.get(0))) {
//        tags.add(attrName + SUBSTRING);
//        if (sortedByCard.get(1).contains(" ") || sortedByCard.get(1).contains("-")) {
//          tags.add(attrName + EXTENDED);
//        }
//      }

    }
    tags.add(TagUtils.create(recordPair, null, diffCount == 0 ? ALL_EQUAL : ALL_WITH_DIFFS));
    if (diffCount > 0) {
      tags.add(TagUtils.create(recordPair, null, ATTR_DIFFCOUNT, String.valueOf(diffCount), (double)diffCount));
    }

    return tags;
  }

}

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

import de.unileipzig.dbs.pprl.core.analyzer.tags.attributepair.StringDistanceAttributePairAnalyzer;
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import de.unileipzig.dbs.pprl.core.common.monitoring.Tag;
import de.unileipzig.dbs.pprl.core.matcher.TagUtils;

import java.util.*;
import java.util.stream.Collectors;

public class PlainRecordPairAnalyzer implements RecordPairAnalyzer {

  public static final String TAG_PREFIX = "PLAIN";
  public static final String ALL_EQUAL = TAG_PREFIX + "_ALL_EQUAL";
  public static final String ALL_WITH_DIFFS = TAG_PREFIX + "_ALL_WITH_DIFFS";
  public static final String ATTR_DIFFCOUNT = TAG_PREFIX + "ATTR_DIFFCOUNT";
  public static final String MISSING = TAG_PREFIX + "_MISSING";
  public static final String MISSING_BOTH = MISSING + "_BOTH";
  public static final String MISSING_ONE = MISSING + "_ONE";
  public static final String SUBSTRING = TAG_PREFIX + "_SUBSTRING";
  public static final String EXTENDED = TAG_PREFIX + "_EXTENDED";
  public static final String EQUAL = TAG_PREFIX + "_EQUAL";
  public static final String UNEQUAL = TAG_PREFIX + "_UNEQUAL";
  public static final String RECORD_DIFF_SCHEMA = TAG_PREFIX + "_DIFF_SCHEMA";

//  StringLengthAttributeAnalyzer sl = new StringLengthAttributeAnalyzer();
//    StringLengthDiffAttributePairAnalyzer sld = new StringLengthDiffAttributePairAnalyzer();
  StringDistanceAttributePairAnalyzer sd = new StringDistanceAttributePairAnalyzer();
//  StringFrequencyAttributePairAnalyzer sf = StringFrequencyAttributePairAnalyzer.createFromFile(
//    "dataset/analysis/all/AttributeMostFrequent"
//  );

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
        continue;
      }
      if (isEmpty(optAttr0) || isEmpty(optAttr1)) {
        tags.add(TagUtils.create(recordPair, attrName, MISSING_ONE));
        diffCount++;
        continue;
      }
      String attr0 = optAttr0.get().getAsString();
      String attr1 = optAttr1.get().getAsString();

      //StringLength
//      tags.addAll(sl.getTags(attrName, attr0, attr1));

      // String Frequency
//      tags.addAll(sf.getTags(attrName, attr0, attr1));

      if (attr0.equals(attr1)) {
        tags.add(TagUtils.create(recordPair, attrName, EQUAL));
        continue;
      }
      tags.add(TagUtils.create(recordPair, attrName, UNEQUAL));
      diffCount++;

      //StringDistance
      for (Tag tag : sd.getTags(attrName, attr0, attr1)) {
        tags.add(TagUtils.addIDs(tag, recordPair));
      }

      //StringLengthDiff
//      sld.getTags(attrName, attr0, attr1).forEach(tag -> tags.add(Tag.addIDs(tag, recordPair)));

      List<String> sortedByLength = Arrays.asList(attr0, attr1);
      sortedByLength.sort(Comparator.comparingInt(String::length));
      if (sortedByLength.get(1).contains(sortedByLength.get(0))) {
        tags.add(TagUtils.create(recordPair, attrName, SUBSTRING));
        if (sortedByLength.get(1).contains(" ") || sortedByLength.get(1).contains("-")) {
          tags.add(TagUtils.create(recordPair, attrName, EXTENDED));
        }
      }

    }
    tags.add(TagUtils.create(recordPair, null, diffCount == 0 ? ALL_EQUAL : ALL_WITH_DIFFS));
    if (diffCount > 0) {
      tags.add(TagUtils.create(recordPair, null, ATTR_DIFFCOUNT, String.valueOf(diffCount), (double) diffCount));
    }

    StringBuilder recordDiffSchema = new StringBuilder();
    for (String attrName : attributeNames) {
      String attrSchemaMarker = "ss";
      if (contains(tags, attrName, EQUAL)) {
        attrSchemaMarker = "==";
      } else if (contains(tags, attrName, MISSING_BOTH)) {
        attrSchemaMarker = "??";
      } else if (contains(tags, attrName, MISSING_ONE)) {
        attrSchemaMarker = "?.";
      } else if (contains(tags, attrName, StringDistanceAttributePairAnalyzer.MAJORDIST)) {
        attrSchemaMarker = "DD";
      } else if (contains(tags, attrName, StringDistanceAttributePairAnalyzer.MINORDIST)) {
        attrSchemaMarker = "SS";
      }
      if (!recordDiffSchema.isEmpty()) {
        recordDiffSchema.append("_");
      }
      recordDiffSchema.append(attrName).append(":").append(attrSchemaMarker);
    }
    tags.add(TagUtils.create(recordPair, null, RECORD_DIFF_SCHEMA, recordDiffSchema.toString(), null));
    tags = tags.stream()
            .filter(t -> !List.of(EQUAL, StringDistanceAttributePairAnalyzer.MAJORDIST, StringDistanceAttributePairAnalyzer.MINORDIST).contains(t.getTag()))
            .toList();
    return tags;
  }


  public static boolean contains(Collection<Tag> tags, String attribute, String tag) {
    return tags.stream().anyMatch(t -> attribute.equals(t.getAttribute()) && tag.equals(t.getTag()));
  }
}

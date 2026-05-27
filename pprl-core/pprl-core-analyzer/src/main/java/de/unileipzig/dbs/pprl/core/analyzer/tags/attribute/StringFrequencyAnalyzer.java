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
import de.unileipzig.dbs.pprl.core.common.frequencies.AttributesFrequencyLookup;
import de.unileipzig.dbs.pprl.core.common.frequencies.CsvAttributesFrequencyLookupProvider;
import de.unileipzig.dbs.pprl.core.common.model.impl.PersonalAttributeType;
import de.unileipzig.dbs.pprl.core.common.monitoring.Tag;

import java.util.*;

import static de.unileipzig.dbs.pprl.core.common.FormattingUtils.roundToDouble;
import static de.unileipzig.dbs.pprl.core.common.FormattingUtils.roundToString;

/**
 * Analyze the frequency of a given attribute value according to a {@link AttributesFrequencyLookup}
 * Generate tags for
 * - most common and rare values
 * - the position of the value in a list of values reverse-sorted by frequency
 * - unknown values
 */
public class StringFrequencyAnalyzer implements AttributeAnalyzer<String> {

  public static final String FREQ = "FREQ";
  public static final String UNKNOWN = FREQ + "_UNKNOWN";
  public static final String SHARE = FREQ + "_SHARE";
  public static final String POSITION_ABSOLUTE = FREQ + "_POS_ABS";
  public static final String POSITION_RELATIVE = FREQ + "_POS_REL";
  public static final String TOP = FREQ + "_IN_TOP_";
  public static final String TOP_ABSOLUTE = TOP + "ABS";
  public static final String TOP_RELATIVE = TOP + "REL";
  public static final String BOTTOM = FREQ + "_IN_BOTTOM_";
  public static final String BOTTOM_ABSOLUTE = BOTTOM + "ABS";
  public static final String BOTTOM_RELATIVE = BOTTOM + "REL";

  private final AttributesFrequencyLookup attributesFrequencyLookup;

//  private final List<Integer> absoluteTopLimits = List.of(10, 20, 100);
  private final List<Integer> absoluteTopLimits = List.of();
  //  private final List<Double> relativeTopLimits = List.of(0.01, 0.05, 0.1, 0.2);
//  private final List<Double> relativeTopLimits = List.of(0.01, 0.05, 0.1, 0.2, 1.0);
  private final List<Double> relativeTopLimits = List.of();
//  private final List<Integer> absoluteBottomLimits = List.of(10, 20, 100);
  private final List<Integer> absoluteBottomLimits = List.of();
//  private final List<Double> relativeBottomLimits = List.of(0.05, 0.2, 0.8, 0.95);
  private final List<Double> relativeBottomLimits = List.of();
  private final Map<String, List<String>> sortedAttributesValues = new HashMap<>();

  public StringFrequencyAnalyzer(
    AttributesFrequencyLookup attributesFrequencyLookup) {
    this.attributesFrequencyLookup = attributesFrequencyLookup;
  }

  @Override
  public List<Tag> getTags(String attrName, String attrValue) {
    attrValue = attributesFrequencyLookup.normalizeAttributeValue(attrName, attrValue);

    Optional<Long> frequency = attributesFrequencyLookup.getFrequency(attrName, attrValue);
    if (frequency.isEmpty()) {
      return List.of(Tag.create(attrName, UNKNOWN, null, null));
    }

    if (!sortedAttributesValues.containsKey(attrName)) {
      sortedAttributesValues.put(attrName,
        attributesFrequencyLookup.getAttributeFrequencyLookup(attrName).get()
          .getAttributesReverseSortedByFrequency());
    }
    List<Tag> tags = new ArrayList<>();

    Long totalCount = attributesFrequencyLookup.getTotalCount(attrName);
    double share = (double)frequency.get() / totalCount;
    tags.add(Tag.create(attrName, SHARE, roundToString(share, 4), roundToDouble(share, 4)));

    int absPos = sortedAttributesValues.get(attrName).indexOf(attrValue);
    // No check for -1 needed, as the that is done
    tags.add(Tag.create(attrName, POSITION_ABSOLUTE, String.valueOf(absPos), (double)absPos));
    int uniqueCount = sortedAttributesValues.get(attrName).size();
    double relPos = (double)absPos / uniqueCount;

    tags.add(Tag.create(attrName, POSITION_RELATIVE, roundToString(relPos, 4),
      roundToDouble(relPos, 4)));

    for (Integer absoluteTopLimit : absoluteTopLimits) {
      if (absPos <= absoluteTopLimit) {
        tags.add(Tag.create(attrName, TOP_ABSOLUTE, FormattingUtils.roundToString(absoluteTopLimit, 0),
          (double)absoluteTopLimit));
      }
    }
    for (Double relativeTopLimit : relativeTopLimits) {
      if (relPos <= relativeTopLimit) {
        tags.add(Tag.create(attrName, TOP_RELATIVE, FormattingUtils.roundToString(relativeTopLimit, 6),
          relativeTopLimit));
      }
    }

    for (Integer absoluteBottomLimit : absoluteBottomLimits) {
      if (absPos >= uniqueCount - absoluteBottomLimit) {
        tags.add(Tag.create(attrName, BOTTOM_ABSOLUTE, FormattingUtils.roundToString(absoluteBottomLimit, 0),
          (double)absoluteBottomLimit));
      }
    }
    for (Double relativeBottomLimit : relativeBottomLimits) {
      if (relPos >= 1 - relativeBottomLimit) {
        tags.add(Tag.create(attrName, BOTTOM_RELATIVE, FormattingUtils.roundToString(relativeBottomLimit, 6),
          relativeBottomLimit));
      }
    }
    return tags;
  }

  public static StringFrequencyAnalyzer createFromFile(String location) {
    CsvAttributesFrequencyLookupProvider provider = new CsvAttributesFrequencyLookupProvider(location);
    provider.setAttributeNamesToParse(List.of(
      PersonalAttributeType.FIRSTNAME.asString(),
      PersonalAttributeType.LASTNAME.asString(),
      PersonalAttributeType.PLZ.asString(),
      PersonalAttributeType.CITY.asString()
    ));
    return new StringFrequencyAnalyzer(
      provider.provide()
    );
  }

}

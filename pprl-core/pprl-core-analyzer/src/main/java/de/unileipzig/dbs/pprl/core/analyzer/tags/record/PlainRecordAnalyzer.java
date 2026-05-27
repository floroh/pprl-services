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

package de.unileipzig.dbs.pprl.core.analyzer.tags.record;

import de.unileipzig.dbs.pprl.core.analyzer.tags.attribute.StringFrequencyAnalyzer;
import de.unileipzig.dbs.pprl.core.analyzer.tags.attribute.StringLengthAttributeAnalyzer;
import de.unileipzig.dbs.pprl.core.common.frequencies.AttributesFrequencyLookup;
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.monitoring.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlainRecordAnalyzer implements RecordAnalyzer {

  public static final String TAG_PREFIX = "PLAIN";
  public static final String MISSING = TAG_PREFIX + "_MISSING";

  StringLengthAttributeAnalyzer sl = new StringLengthAttributeAnalyzer();
  StringFrequencyAnalyzer sf;

  @Override
  public List<Tag> getTags(Record record) {
    List<Tag> tags = new ArrayList<>();
    tags.addAll(new RecordLengthAnalyzer().getTags(record));
    for (String attrName : record.getAttributeNames()) {
      Optional<Attribute> optAttr = record.getAttribute(attrName);
      if (isEmpty(optAttr)) {
        tags.add(Tag.create(record, attrName, MISSING));
        continue;
      }
      String attr = optAttr.get().getAsString();

      //StringLength
      sl.getTags(attrName, attr).forEach(tag -> tags.add(tag.addID(record)));

      // String Frequency
      if (sf != null) {
        sf.getTags(attrName, attr).forEach(tag -> tags.add(tag.addID(record)));
      }
    }
    return tags;
  }

  public void useAttributeFrequencyLookup(AttributesFrequencyLookup afl) {
    sf = new StringFrequencyAnalyzer(afl);
  }

  public void setSf(StringFrequencyAnalyzer sf) {
    this.sf = sf;
  }
}

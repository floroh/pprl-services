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

package de.unileipzig.dbs.pprl.service.protocol.analysis.aggregation;

import tech.tablesaw.api.Row;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import static de.unileipzig.dbs.pprl.core.analyzer.tags.recordpair.PlainRecordPairAnalyzer.ALL_EQUAL;
import static de.unileipzig.dbs.pprl.core.analyzer.tags.recordpair.PlainRecordPairAnalyzer.ATTR_DIFFCOUNT;
import static de.unileipzig.dbs.pprl.core.common.monitoring.TagTable.LINK_TAG;

public class NumberOfAttributesWithErrors extends TaggedResultAggregator {

  public static final String CRITERIA = "Diff. attr / record";

  @Override
  public Table aggregate(Table taggedLinks) {
    Table byTag = aggregateByTag(taggedLinks);
    return aggregateFromByTag(byTag);
  }

  public Table aggregateFromByTag(Table byTag) {
    Table tmp = byTag.where(
      byTag.stringColumn(LINK_TAG).startsWith(ATTR_DIFFCOUNT)
        .or(byTag.stringColumn(LINK_TAG).isEqualTo(ALL_EQUAL))
    ).sortOn(0, 1);
//        System.out.println(tmp.printAll());

    Table byDiffAttr = getResultTable(
      StringColumn.create(CRITERIA)
    ).setName("ByNumberOfDifferingAttributes");

    tmp.splitOn(LINK_TAG).getSlices().forEach(slice -> {
      int err = -1;

      String tag = slice.stringColumn(LINK_TAG).get(0);
      if (tag.equals(ALL_EQUAL)) {
        err = 0;
      } else {
        err = Integer.parseInt(
          tag.substring(tag.lastIndexOf("_") + 1)
        );
      }
      Row newRow = appendResultRow(byDiffAttr, slice);
      newRow.setString(0, Integer.toString(err));
    });
    return byDiffAttr;
  }

  @Override
  public String getCriteriaColumnName() {
    return CRITERIA;
  }
}

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

import de.unileipzig.dbs.pprl.core.analyzer.tags.recordpair.PlainRecordPairAnalyzer;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import static de.unileipzig.dbs.pprl.core.common.monitoring.TagTable.LINK_TAG;

public class RecordComparisonSchema extends TaggedResultAggregator {

  public static final String CRITERIA = "Schema";

  @Override
  public Table aggregate(Table taggedLinks) {
    Table byTag = aggregateByTag(taggedLinks);
    return aggregateFromByTag(byTag);
  }

  public Table aggregateFromByTag(Table byTag) {
    Table tmp = filterByTagTable(byTag);
    System.out.println(tmp.setName("byTagFilteredOnRecordSchema").printAll());

    Table result = getResultTable(
      StringColumn.create(CRITERIA)
    ).setName("ByRecordComparisonSchema");

    tmp.splitOn(LINK_TAG).getSlices().forEach(slice -> {
      String tag = slice.stringColumn(LINK_TAG).get(0);
      Row newRow = appendResultRow(result, slice);
      newRow.setString(0, tag);
    });
    return result.sortOn("FP", "FN", "TP");
//    return result.sortOn("TP", "FP", "FN");
  }

  public static Table filterByTagTable(Table byTag) {
    return byTag.where(byTag.stringColumn(LINK_TAG).containsString(PlainRecordPairAnalyzer.RECORD_DIFF_SCHEMA))
      .sortOn(0, 1);
  }

  @Override
  public String getCriteriaColumnName() {
    return CRITERIA;
  }
}

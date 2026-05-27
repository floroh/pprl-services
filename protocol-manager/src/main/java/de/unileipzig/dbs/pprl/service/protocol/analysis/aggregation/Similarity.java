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

import static de.unileipzig.dbs.pprl.core.common.monitoring.TagTable.LINK_TAG;
import static de.unileipzig.dbs.pprl.service.protocol.analysis.AnalyzerUtils.PAIRSIM;

public class Similarity extends TaggedResultAggregator {

  public static final String CRITERIA = "Record similarity";

  @Override
  public Table aggregate(Table taggedLinks) {
    Table byTag = aggregateByTag(taggedLinks);
    return aggregateFromByTag(byTag);
  }

  public Table aggregateFromByTag(Table byTag) {
    Table tmp = filterByTagTable(byTag);

    Table result = getResultTable(
      StringColumn.create(CRITERIA)
    ).setName("ByRecordSimilarity");

    tmp.splitOn(LINK_TAG).getSlices().forEach(slice -> {
      String tag = slice.stringColumn(LINK_TAG).get(0);
      String sim = tag.substring(PAIRSIM.length());
      Row newRow = appendResultRow(result, slice);
      newRow.setString(0, sim);
    });
    return result.sortOn(0);
  }

  public static Table filterByTagTable(Table byTag) {
    return byTag.where(byTag.stringColumn(LINK_TAG).containsString(PAIRSIM))
      .sortOn(0, 1);
  }

  @Override
  public String getCriteriaColumnName() {
    return CRITERIA;
  }
}

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

import de.unileipzig.dbs.pprl.service.dataowner.modifier.attribute.*;
import de.unileipzig.dbs.pprl.service.dataowner.modifier.record.*;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import static de.unileipzig.dbs.pprl.core.analyzer.tags.attributepair.StringDistanceAttributePairAnalyzer.MAJORDIST;
import static de.unileipzig.dbs.pprl.core.analyzer.tags.attributepair.StringDistanceAttributePairAnalyzer.MINORDIST;
import static de.unileipzig.dbs.pprl.core.analyzer.tags.record.PlainRecordAnalyzer.MISSING;
import static de.unileipzig.dbs.pprl.core.analyzer.tags.recordpair.FamilyAnalyzer.FAMILY;
import static de.unileipzig.dbs.pprl.core.analyzer.tags.recordpair.PlainRecordPairAnalyzer.EXTENDED;
import static de.unileipzig.dbs.pprl.core.analyzer.tags.recordpair.PlainRecordPairAnalyzer.SUBSTRING;
import static de.unileipzig.dbs.pprl.core.common.monitoring.TagTable.LINK_TAG;


public class ErrorType extends TaggedResultAggregator {

  public static final String CRITERIA = "Error type";

  @Override
  public Table aggregate(Table taggedLinks) {
    Table byTag = aggregateByTag(taggedLinks);
    return aggregateFromByTag(byTag);
  }

  public Table aggregateFromByTag(Table byTag) {
    Table tmp = byTag.where(containsStringSelection(byTag.stringColumn(LINK_TAG),
        MISSING,
        MINORDIST,
        AttributeReplacer.TAG_POSTFIX,
        CharSwapper.TAG_POSTFIX,
        CharInserter.TAG_POSTFIX,
        CharReplacer.TAG_POSTFIX,
        DateTypoModifier.TAG_POSTFIX,
        MultiFieldAdder.TAG_POSTFIX,
        AttributeCopy.TAG_POSTFIX,
        AttributeCopyReplace.TAG_POSTFIX,
        AttributeEmptier.TAG_POSTFIX,
        AttributeRemover.TAG_POSTFIX,
        AttributeSwapper.TAG_POSTFIX,
        MAJORDIST,
        SUBSTRING,
        EXTENDED,
        FAMILY))
        .sortOn(0, 1);
//        System.out.println(tmp.printAll());

    Table result = getResultTable(
      StringColumn.create(CRITERIA)
    ).setName("ByBasicErrorType");

    tmp.splitOn(LINK_TAG).getSlices().forEach(slice -> {
      String tag = slice.stringColumn(LINK_TAG).get(0);
      Row newRow = appendResultRow(result, slice);
      newRow.setString(0, tag);
    });
    return result;
  }

  @Override
  public String getCriteriaColumnName() {
    return CRITERIA;
  }
}

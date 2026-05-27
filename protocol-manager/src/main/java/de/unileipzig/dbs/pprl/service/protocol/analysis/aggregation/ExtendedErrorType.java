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

import de.unileipzig.dbs.pprl.core.common.model.impl.PersonalAttributeType;
import de.unileipzig.dbs.pprl.core.matcher.evaluation.GroundTruth;
import de.unileipzig.dbs.pprl.core.matcher.evaluation.QualityCheck;
import de.unileipzig.dbs.pprl.service.dataowner.modifier.attribute.*;
import de.unileipzig.dbs.pprl.service.dataowner.modifier.record.AttributeEmptier;
import de.unileipzig.dbs.pprl.service.dataowner.modifier.record.AttributeRemover;
import de.unileipzig.dbs.pprl.service.dataowner.modifier.record.AttributeSwapper;
import de.unileipzig.dbs.pprl.service.protocol.analysis.AnalyzerUtils;
import tech.tablesaw.aggregate.AggregateFunctions;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.selection.Selection;

import java.util.Map;

import static de.unileipzig.dbs.pprl.core.analyzer.tags.attributepair.StringDistanceAttributePairAnalyzer.MAJORDIST;
import static de.unileipzig.dbs.pprl.core.analyzer.tags.attributepair.StringDistanceAttributePairAnalyzer.MINORDIST;
import static de.unileipzig.dbs.pprl.core.analyzer.tags.recordpair.FamilyAnalyzer.FAMILY;
import static de.unileipzig.dbs.pprl.core.analyzer.tags.recordpair.PlainRecordPairAnalyzer.EXTENDED;
import static de.unileipzig.dbs.pprl.core.analyzer.tags.recordpair.PlainRecordPairAnalyzer.MISSING_ONE;


public class ExtendedErrorType extends TaggedResultAggregator {

  public static final String CRITERIA = "Error type";

  @Override
  public Table aggregate(Table results) {

    Table combinedResults = AnalyzerUtils.addCombinedTag(results);

    Map<String, Selection> selections = getSelections(combinedResults);

    Table result = getResultTable(
      StringColumn.create(CRITERIA)
    ).setName("ByErrorType");

    for (Map.Entry<String, Selection> stringSelectionEntry : selections.entrySet()) {
      Table tmp = combinedResults.where(stringSelectionEntry.getValue()).sortOn(0, 1);
//            System.out.println(stringSelectionEntry.getKey() + ": " + tmp.rowCount());
      if (tmp.rowCount() == 0) {
        continue;
      }

      tmp = tmp.selectColumns(GroundTruth.LEFT_ID, GroundTruth.RIGHT_ID, QualityCheck.LINK_LABEL);
//            System.out.println(tmp.print());
      Table byTag =
        tmp.summarize(QualityCheck.LINK_LABEL, AggregateFunctions.count).by(QualityCheck.LINK_LABEL);
//            System.out.println(byTag.print());
      Row newRow = appendResultRow(result, byTag);
      newRow.setString(0, stringSelectionEntry.getKey());
    }
    return result;
  }

  public static Map<String, Selection> getSelections(Table combinedTagResults) {
    StringColumn col = combinedTagResults.stringColumn(AnalyzerUtils.COMBINED_TAGS);

    return Map.of(
      "EXTENDED", containsStringSelection(
        col,
//        SUBSTRING,
        EXTENDED,
        MultiFieldAdder.TAG_POSTFIX
      ),
      "MISSING", containsStringSelection(
        col,
        MISSING_ONE,
        AttributeRemover.TAG_POSTFIX,
        AttributeEmptier.TAG_POSTFIX
      ),
      "REPLACED", containsStringSelection(col, MAJORDIST, AttributeReplacer.TAG_POSTFIX),
      "FAMILY", containsStringSelection(col, FAMILY),
      "MOVED", containsStringSelection(col, PersonalAttributeType.ADDRESS.name() + MAJORDIST),
      "EXCHANGED", containsStringSelection(col, AttributeSwapper.TAG_POSTFIX),
      "MINOR", containsStringSelection(
        col,
        MINORDIST,
        CharSwapper.TAG_POSTFIX,
        CharInserter.TAG_POSTFIX,
        CharReplacer.TAG_POSTFIX,
        DateTypoModifier.TAG_POSTFIX
      )
    );
  }

  @Override
  public String getCriteriaColumnName() {
    return CRITERIA;
  }
}

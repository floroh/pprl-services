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

import de.unileipzig.dbs.pprl.core.matcher.evaluation.QualityCheck;
import de.unileipzig.dbs.pprl.core.matcher.evaluation.QualityResult;
import tech.tablesaw.aggregate.AggregateFunctions;
import tech.tablesaw.api.*;
import tech.tablesaw.columns.Column;
import tech.tablesaw.selection.Selection;
import tech.tablesaw.table.TableSlice;

import java.util.List;
import java.util.Optional;

import static de.unileipzig.dbs.pprl.core.common.monitoring.TagTable.LINK_TAG;
import static de.unileipzig.dbs.pprl.core.matcher.evaluation.QualityCheck.*;
import static de.unileipzig.dbs.pprl.service.protocol.analysis.AnalyzerUtils.OVERALL;

public abstract class TaggedResultAggregator {

  public abstract Table aggregate(Table taggedLinks);

  public abstract String getCriteriaColumnName();

  public static Table aggregateByTag(Table taggedLinks) {
    Table byTag = taggedLinks.summarize(LINK_TAG, AggregateFunctions.count).by(
      LINK_TAG, QualityCheck.LINK_LABEL);
    byTag = byTag.sortOn(0, 1).setName("ByTag");
    return byTag;
  }

  public static Table aggregatedResultToRelative(Table aggregatedResultWithOverall) {
    Table out = aggregatedResultWithOverall.copy();
    List<String> proportionalColumns = List.of(QualityCheck.Label.TP.toString(), QualityCheck.Label.FP.toString(), QualityCheck.Label.FN.toString(),
      QualityCheck.Label.FPs.toString(), QualityCheck.Label.FPd.toString()
    );
    List<String> diffColumns = List.of(F1, RECALL, PRECISION);
    Optional<Row> optOverallRow =
      out.where(out.stringColumn(0).isEqualTo(OVERALL))
        .stream().findAny();
    if (optOverallRow.isEmpty()) {
      throw new RuntimeException("Missing row with overall results in " + out);
    }
    Row overallRow = optOverallRow.get();
    final List<String> columnNames = overallRow.columnNames();
    columnNames.removeFirst();

    out.stream().forEach(row -> {
      if (row.getRowNumber() == out.rowCount() - 1) return;
      for (String columnName : columnNames) {
        if (proportionalColumns.contains(columnName)) {
          long rowValue = row.getLong(columnName);
          long overallValue = overallRow.getLong(columnName);
          row.setLong(columnName, Math.round(100.0 * rowValue/overallValue));
        } else if (diffColumns.contains(columnName)) {
          double rowValue = row.getDouble(columnName);
          double overallValue = overallRow.getDouble(columnName);
          row.setDouble(columnName, QualityResult.roundAsDouble(rowValue - overallValue));
        }
      }
    });
    return out;
  }

  public static Table getResultTable(Column<?> firstColumn) {
    return Table.create(
      "Result table",
      firstColumn,
      DoubleColumn.create(RECALL),
      DoubleColumn.create(PRECISION),
      DoubleColumn.create(F1),
      LongColumn.create(Label.TP.toString()),
      LongColumn.create(Label.FP.toString()),
      LongColumn.create(Label.FN.toString()),
      LongColumn.create(Label.FPs.toString()),
      LongColumn.create(Label.FPd.toString())
    );
  }

  public static Row appendResultRow(Table tab, TableSlice slice) {
    QualityResult res = new QualityResult();
    slice.asTable().stream().forEach(r -> {
      long val = Math.round(r.getDouble("Count [" + LINK_TAG + "]"));
      switch (r.getString(QualityCheck.LINK_LABEL)) {
        case "TP":
          res.setTruePos(val);
          break;
        case "FP":
          res.setFalsePos(val);
          break;
        case "FPs":
          res.setFalsePosSingleton(val);
          res.setFalsePos(res.getFalsePos() + val);
          break;
        case "FPd":
          res.setFalsePosDuplicates(val);
          res.setFalsePos(res.getFalsePos() + val);
          break;
        case "FN":
          res.setFalseNeg(val);
          break;
      }
    });

    return appendResultRow(tab, res);
  }

  public static Row appendResultRow(Table results, Table typeTable) {
    QualityResult res = getQualityResult(typeTable);
    return appendResultRow(results, res);
  }

  public static QualityResult getQualityResult(Table typeTable) {
    QualityResult res = new QualityResult();
    typeTable.forEach(r -> {
      long val = Math.round(r.getDouble("Count [" + LINK_LABEL + "]"));
      switch (r.getString(QualityCheck.LINK_LABEL)) {
        case "TP":
          res.setTruePos(val);
          break;
        case "FP":
          res.setFalsePos(val);
          break;
        case "FPs":
          res.setFalsePosSingleton(val);
          res.setFalsePos(res.getFalsePos() + val);
          break;
        case "FPd":
          res.setFalsePosDuplicates(val);
          res.setFalsePos(res.getFalsePos() + val);
          break;
        case "FN":
          res.setFalseNeg(val);
          break;
      }
    });
    return res;
  }

  public static Row appendResultRow(Table results, QualityResult res) {
    Row newRow = results.appendRow();
    newRow.setDouble(1, QualityResult.roundAsDouble(res.getRecall()));
    newRow.setDouble(2, QualityResult.roundAsDouble(res.getPrecision()));
    newRow.setDouble(3, QualityResult.roundAsDouble(res.getF1Score()));
    newRow.setLong(4, res.getTruePos());
    newRow.setLong(5, res.getFalsePos());
    newRow.setLong(6, res.getFalseNeg());
    newRow.setLong(7, res.getFalsePosSingleton());
    newRow.setLong(8, res.getFalsePosDuplicates());
    return newRow;
  }

  public static Selection containsStringSelection(StringColumn col, String... tags) {
    Selection selection = null;
    for (String tag : tags) {
      if (selection == null) {
        selection = col.containsString(tag);
      } else {
        selection = selection.or(col.containsString(tag));
      }
    }
    return selection;
  }
}

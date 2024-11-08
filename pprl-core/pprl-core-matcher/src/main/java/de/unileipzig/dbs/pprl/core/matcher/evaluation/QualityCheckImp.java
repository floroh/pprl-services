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

package de.unileipzig.dbs.pprl.core.matcher.evaluation;

import de.unileipzig.dbs.pprl.core.common.RecordUtils;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordId;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordIdPair;
import de.unileipzig.dbs.pprl.core.common.model.impl.RecordIdComposed;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.tablesaw.aggregate.AggregateFunctions;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.selection.Selection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class QualityCheckImp implements QualityCheck {

  private GroundTruth gt;

  private Logger logger = LogManager.getLogger();

  public QualityCheckImp(GroundTruth gt) {
    this.gt = gt;
  }

  @Override
  public ExtendedQualityResult evaluate(Collection<RecordId> recordIds) {
    return evaluateLinks(GroundTruth.linksFromGlobalRecordIds(recordIds));
  }

  @Override
  public ExtendedQualityResult evaluatePairs(Collection<RecordIdPair> idPairs) {
    return evaluateLinks(GroundTruth.linksFromRecordPairs(idPairs));
  }

  ExtendedQualityResult evaluateLinks(Table links) {
    logger.debug("Starting quality check...");
    logger.debug("Number of links: " + links.rowCount());

    Table result = gt.getExpectedLinks();
    result.insertColumn(2, StringColumn.create(
      LINK_LABEL,
      Collections.nCopies(result.rowCount(), Label.FN.name())
    ));
    logger.debug("Building expectedPairIdToRowNumber");
    Map<String, Integer> expectedPairIdToRowNumber = result.stream()
      .collect(Collectors.toMap(
        row -> RecordUtils.getPairId(
          row.getString(GroundTruth.LEFT_ID), row.getString(GroundTruth.RIGHT_ID)),
        Row::getRowNumber
      ));

    List<String> ids = new ArrayList<>();
    ids.addAll(result.stringColumn(GroundTruth.LEFT_ID).asList());
    ids.addAll(result.stringColumn(GroundTruth.RIGHT_ID).asList());

    logger.debug("Building srcIndexedGlobalIds");
    Map<String, Set<String>> srcIndexedGlobalIds = new HashMap<>();
    for (String id : ids) {
      String src = RecordIdComposed.ofComposed(id).getSourceId();
      if (!srcIndexedGlobalIds.containsKey(src)) {
        srcIndexedGlobalIds.put(src, new HashSet<>());
      }
      srcIndexedGlobalIds.get(src).add(id);
    }
    Table extRealLinks = links.copy();
    extRealLinks.insertColumn(
      2,
      StringColumn.create(LINK_LABEL, Collections.nCopies(extRealLinks.rowCount(), Label.FP.name()))
    );

    Table additionalLinks = result.emptyCopy();
    logger.debug("Start evaluating links");
    extRealLinks.stream()
      .forEach(row -> {
        String leftId = row.getString(GroundTruth.LEFT_ID);
        String rightId = row.getString(GroundTruth.RIGHT_ID);
        String pairId = RecordUtils.getPairId(leftId, rightId);
        boolean isUnexpectedLink = !expectedPairIdToRowNumber.containsKey(pairId);
        if (isUnexpectedLink) {
          //TODO Check for difference between FPs and FPd
          // requires access to source-indexed global ids (Map<SRC, List<GLOBAL_ID>>
          // if map.get(SRC).contains(GLOBAL_ID)
          String leftSrc = RecordIdComposed.ofComposed(leftId).getSourceId();
          String rightSrc = RecordIdComposed.ofComposed(rightId).getSourceId();
          if (
            (srcIndexedGlobalIds.containsKey(leftSrc) && srcIndexedGlobalIds.get(leftSrc).contains(leftId)) ||
              (srcIndexedGlobalIds.containsKey(rightSrc) &&
                srcIndexedGlobalIds.get(rightSrc).contains(rightId))) {
            row.setString(LINK_LABEL, Label.FPd.toString());
          } else {
            row.setString(LINK_LABEL, Label.FPs.toString());
          }
          additionalLinks.addRow(row);
        } else {
          Row curRow = result.row(expectedPairIdToRowNumber.get(pairId));
          curRow.setString(2, Label.TP.toString());
        }
      });
    result.append(additionalLinks);
//        System.out.println(result.summary().printAll());

    logger.debug("Aggregating result...");
    Table aggregatedResult = result.summarize(LINK_LABEL, AggregateFunctions.count).by(LINK_LABEL);
//		System.out.println(aggregatedResult);

    ExtendedQualityResult evaluationResult = getExtendedQualityResult(aggregatedResult);
    evaluationResult.setResults(result);
    logger.info("Finished quality check: " + evaluationResult);
    return evaluationResult;
  }

  private ExtendedQualityResult getExtendedQualityResult(Table aggregatedResult) {
    long tp = 0;
    long fp = 0;
    long fn = 0;
    long fpd = 0;
    long fps = 0;
    for (Row row : aggregatedResult) {
      long val = (int) row.getDouble(1);
      Label lbl = Label.valueOf(row.getString(0));
      switch (lbl) {
        case FN:
          fn = val;
          break;
        case FP:
          fp = val;
          break;
        case FPd:
          fpd = val;
          fp += val;
          break;
        case FPs:
          fps = val;
          fp += val;
          break;
        case TP:
          tp = val;
          break;
      }
    }
    ExtendedQualityResult evaluationResult = new ExtendedQualityResult(tp, fp, fn);
    evaluationResult.setFalsePosDuplicates(fpd);
    evaluationResult.setFalsePosSingleton(fps);
    return evaluationResult;
  }

  private Selection checkIfLinkIsInTable(Table tab, String s0, String s1) {
    Selection sel = (tab.stringColumn(0).isEqualTo(s0)
      .and(tab.stringColumn(1).isEqualTo(s1)))
      .or(tab.stringColumn(1).isEqualTo(s0)
        .and(tab.stringColumn(0).isEqualTo(s1)));
    return sel;
  }

}

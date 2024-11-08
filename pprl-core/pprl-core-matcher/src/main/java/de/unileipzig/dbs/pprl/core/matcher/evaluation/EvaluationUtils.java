package de.unileipzig.dbs.pprl.core.matcher.evaluation;

import de.unileipzig.dbs.pprl.core.common.RecordUtils;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import de.unileipzig.dbs.pprl.core.common.model.impl.MatchGrade;
import de.unileipzig.dbs.pprl.core.common.monitoring.Tag;
import de.unileipzig.dbs.pprl.core.matcher.classification.Classifier;
import tech.tablesaw.api.Table;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EvaluationUtils {

  public static void addGroundTruthTags(List<RecordPair> pairs, GroundTruth groundTruth) {
    QualityCheckImp qualityCheckImp = new QualityCheckImp(groundTruth);
    List<RecordPair> remainingPairs =
      pairs;
//        pairs.stream()
//          .filter(pair -> pair.getClassification().isAtLeast(MatchGrade.PROBABLE_MATCH))
//          .collect(Collectors.toList());
    ExtendedQualityResult extendedQualityResult =
      (ExtendedQualityResult) qualityCheckImp.evaluateRecordPairs(remainingPairs);
    Table results = extendedQualityResult.getResults();
    Map<String, String> pairIdToLinkLabel = new HashMap<>();
    results.forEach(row -> {
      String pairId =
        RecordUtils.getPairId(row.getString(GroundTruth.LEFT_ID), row.getString(GroundTruth.RIGHT_ID));
      pairIdToLinkLabel.put(pairId, row.getString(QualityCheck.LINK_LABEL));
    });
    for (RecordPair pair : pairs) {
      String label = pairIdToLinkLabel.get(pair.getPairId());
      if (label == null) {
//          label = "TN";
        label = "UNKNOWN";
      }
      // Fix the labels according to the match grade
      if (!pair.getClassification().isAtLeast(MatchGrade.PROBABLE_MATCH)) {
        if (label.contains("FP")) {
          label = "TN";
        }
        if (label.equals("TP")) {
          label = "FN";
        }
      }
      String gtLabel = List.of("TP", "FN").contains(label)?
        Classifier.Label.TRUE_MATCH.name()
        : Classifier.Label.TRUE_NON_MATCH.name();
      pair.addTag(
        Tag.create("Groundtruth-Label", gtLabel,
          gtLabel.equals(Classifier.Label.TRUE_MATCH.name())? 1.0 : 0.0)
      );
      pair.addTag(
        Tag.create(QualityCheck.LINK_LABEL, label, List.of("TP", "TN").contains(label)? 1.0 : 0.0)
      );
    }
  }

}

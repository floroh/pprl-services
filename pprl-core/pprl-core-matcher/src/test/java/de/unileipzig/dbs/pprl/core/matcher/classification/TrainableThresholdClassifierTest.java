package de.unileipzig.dbs.pprl.core.matcher.classification;

import de.unileipzig.dbs.pprl.core.matcher.evaluation.QualityResult;
import org.junit.jupiter.api.Test;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TrainableThresholdClassifierTest {

  @Test
  void sortByDistanceToBestThreshold() {
//    TrainableThresholdClassifier classifier = new TrainableThresholdClassifier(0.8);
    List<Double> thresholds = new ArrayList<>(List.of(0.77, 0.78, 0.79, 0.8, 0.81, 0.82, 0.83));
    TrainableThresholdClassifier.sortByDistanceToBestThreshold(thresholds, 0.8);
    System.out.println(thresholds);

  }

  @Test
  void scaledBySimilarityDistribution() {
    Table dist = Table.create("dist").addColumns(
      StringColumn.create("bin", List.of("0.50-0.51", "0.51-0.52", "0.52-0.53", "0.53-0.54", "0.54-0.55",
        "0.55-0.56")),
      DoubleColumn.create("count", 200, 100, 50, 60, 40, 30)
    );
    Table out = TrainableThresholdClassifier.prepareSimilarityDistributionTable(dist);
    System.out.println(out.printAll());

    QualityResult bareResult = new QualityResult(10, 5, 5);
    bareResult.setTrueNeg(10);
    System.out.println(bareResult);
    QualityResult scaledResult =
      new TrainableThresholdClassifier(0.55).scaleQualityResultToGlobalDistribution(bareResult, 0.53, 4, 4,
        dist);
    System.out.println(scaledResult);

    assertTrue(scaledResult.getTrueNeg() > bareResult.getTruePos());
    assertTrue(scaledResult.getFalseNeg() > bareResult.getFalsePos());
  }
}
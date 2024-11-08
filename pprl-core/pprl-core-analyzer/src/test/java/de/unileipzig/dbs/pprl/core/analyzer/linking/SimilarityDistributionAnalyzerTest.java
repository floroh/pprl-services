package de.unileipzig.dbs.pprl.core.analyzer.linking;

import de.unileipzig.dbs.pprl.core.analyzer.results.ResultSet;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static de.unileipzig.dbs.pprl.core.analyzer.linking.SimilarityDistributionAnalyzer.BIN_BOUND_SEPARATOR;
import static de.unileipzig.dbs.pprl.core.analyzer.linking.SimilarityDistributionAnalyzer.COLUMN_BIN_RANGE;
import static org.junit.jupiter.api.Assertions.*;

class SimilarityDistributionAnalyzerTest {

  @Test
  void uniformDistribution() {
    List<Double> uniformDistribution = createUniformDistribution();

    SimilarityDistributionAnalyzer analyzer = new SimilarityDistributionAnalyzer();
    double binSize = 0.04;
    analyzer.setBinSize(binSize);
    ResultSet resultSet = analyzer.analyzeSimilarities(uniformDistribution);
//    System.out.println(resultSet.getAsTable().printAll());

    List<BigDecimal> binCounts = getBinCounts(resultSet);
    assertEquals(1 / binSize, binCounts.size());
    assertEquals(uniformDistribution.size(), binCounts.stream().mapToLong(BigDecimal::longValue).sum());
  }

  @Test
  void testHigherLowerBound() {
    double minSimilarity = 0.6;
    List<Double> uniformDistribution = createUniformDistribution().stream()
      .filter(similarity -> similarity > minSimilarity)
      .collect(Collectors.toList());

    SimilarityDistributionAnalyzer analyzer = new SimilarityDistributionAnalyzer();
    double binSize = 0.05;
    analyzer.setBinSize(binSize);
    double lowerBound = 0.4;
    analyzer.setLowerBound(lowerBound);
    ResultSet resultSet = analyzer.analyzeSimilarities(uniformDistribution);
//    System.out.println(resultSet.getAsTable().printAll());

    List<BigDecimal> binCounts = getBinCounts(resultSet);
    assertEquals(Math.round((1- lowerBound)/ binSize), binCounts.size());
    assertEquals(uniformDistribution.size(), binCounts.stream().mapToLong(BigDecimal::longValue).sum());
  }

  @Test
  void testDynamicLowerBound() {
    double minSimilarity = 0.3;
    List<Double> uniformDistribution = createUniformDistribution().stream()
      .filter(similarity -> similarity > minSimilarity)
      .collect(Collectors.toList());

    SimilarityDistributionAnalyzer analyzer = new SimilarityDistributionAnalyzer();
    double binSize = 0.05;
    analyzer.setBinSize(binSize);
    analyzer.setDynamicLowerBound(true);
    ResultSet resultSet = analyzer.analyzeSimilarities(uniformDistribution);
//    System.out.println(resultSet.getAsTable().printAll());

    List<BigDecimal> binCounts = getBinCounts(resultSet);
    assertEquals(uniformDistribution.size(), binCounts.stream().mapToLong(BigDecimal::longValue).sum());
    assertEquals(minSimilarity,
      Double.parseDouble(resultSet.getResults().getFirst().getParams()
        .get(COLUMN_BIN_RANGE).split(BIN_BOUND_SEPARATOR)[0]));
  }

  private static List<BigDecimal> getBinCounts(ResultSet resultSet) {
    List<BigDecimal> binCounts = resultSet.getResults().stream()
      .map(result -> result.getMetrics().get("count"))
      .toList();
    return binCounts;
  }

  private static List<Double> createUniformDistribution() {
    int numberOfValues = 500;
    return IntStream.range(0, numberOfValues)
      .mapToObj(i -> (double) i / numberOfValues)
      .collect(Collectors.toList());
  }
}
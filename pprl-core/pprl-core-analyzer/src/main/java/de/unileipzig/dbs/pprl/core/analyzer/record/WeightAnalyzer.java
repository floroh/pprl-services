package de.unileipzig.dbs.pprl.core.analyzer.record;

import de.unileipzig.dbs.pprl.core.analyzer.attribute.AttributeAnalyzer;
import de.unileipzig.dbs.pprl.core.analyzer.attribute.AttributeMostFrequent;
import de.unileipzig.dbs.pprl.core.analyzer.cluster.ClusterAnalyzer;
import de.unileipzig.dbs.pprl.core.analyzer.cluster.ClusterPairwiseEqual;
import de.unileipzig.dbs.pprl.core.analyzer.results.Result;
import de.unileipzig.dbs.pprl.core.analyzer.results.ResultSet;
import de.unileipzig.dbs.pprl.core.common.frequencies.WeightUtils;
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import tech.tablesaw.aggregate.AggregateFunctions;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

/**
 * Analyze the discriminatory power of attributes and derive weights
 */
public class WeightAnalyzer extends RecordAnalyzer {

  public static final String M_WEIGHT = "m-weight";
  public static final String U_WEIGHT = "u-weight";
  public static final String NORM_POSTFIX = " (norm)";
  public static final String WEIGHT = "weight";
  private AttributeMostFrequent attributeMostFrequent;

  private ClusterPairwiseEqual clusterPairwiseEqual;

  public WeightAnalyzer() {
    init();
  }

  private void init() {
    attributeMostFrequent = new AttributeMostFrequent();
    attributeMostFrequent.setMinCount(0);

    clusterPairwiseEqual = new ClusterPairwiseEqual();
  }

  @Override
  public ResultSet analyze(List<Record> records) {
    ResultSet resultSet = getResultSet();
    resultSet.setDescription(buildDescription());

    Map<String, List<Attribute>> attributes = AttributeAnalyzer.prepareRecords(records);
    List<String> attributeNames = attributes.keySet()
      .stream()
      .sorted()
      .collect(Collectors.toList());
    if (attributeNames.isEmpty()) {
      return resultSet;
    }
    Map<String, Table> attributeFrequencies =
      attributeMostFrequent.analyze(attributes).getAdditionalResults();

    Map<String, List<Record>> cluster = ClusterAnalyzer.prepareRecords(records);
    if (cluster.isEmpty()) {
      logger.warn("Aborting WeightAnalyzer because there are no duplicates");
      return resultSet;
    }
    Table errorTable = clusterPairwiseEqual.analyze(cluster).getAsTable();
    Map<String, Double> attributeErrors = new HashMap<>();
    errorTable.stream().forEach(r -> {
      String attrName = r.getString(HEADER_ATTRIBUTE);
      Double attrError = r.getDouble("mean");
      attributeErrors.put(attrName, attrError);
    });
//    StringColumn colAttribute = StringColumn.create(HEADER_ATTRIBUTE);
//    DoubleColumn colAvgFrequency = DoubleColumn.create("Avg. frequency");

    for (String attributeName : attributeNames) {
      DoubleColumn colFreq = attributeFrequencies.get(attributeName)
        .doubleColumn(HEADER_RELATIVE_FREQUENCY);
      colFreq.sortDescending();
      double equalityByChance = colFreq.asList().stream().mapToDouble(d -> d)
        .map(d -> d*d)
        .sum();
      OptionalDouble optionalAvgFrequency = colFreq.asList().stream().mapToDouble(d -> d).average();

      if (!attributeErrors.containsKey(attributeName)) {
        logger.info("No error for attribute " + attributeName + " found. Skipping attribute.");
        continue;
      }
      Double avgError = attributeErrors.get(attributeName);
      if (avgError == 0) {
        logger.warn("AvgError of true duplicates for attribute " + attributeName + " is 0 (all equal)." +
          " To be able to calculate weights it is set to 0.01");
        avgError = 0.01;
      } else if (avgError == 1.0) {
        logger.warn("AvgError of true duplicates for attribute " + attributeName + " is 1 (all different)." +
          " To be able to calculate weights it is set to 0.9999");
        avgError = 0.9999;
      }
      double probM = WeightUtils.getProbM(avgError);
//      double probU = WeightUtils.getProbU(optionalAvgFrequency.getAsDouble());
      double probU = WeightUtils.getProbU(equalityByChance);
      if (probU == 1) {
        logger.warn("u-Probability is for attribute " + attributeName + " is 1." +
          " To be able to calculate weights it is set to 0.99");
        probU = 0.99;
      }
      logger.info("p(m) = " + probM + ", p(u) = " + probU);
      double weightM = WeightUtils.getWeightM(probM, probU);
      double weightU = WeightUtils.getWeightU(probM, probU);
      logger.info("w(m) = " + weightM + ", w(u) = " + weightU);
//      System.out.println(attributeName + ": "
//        + "avgError=" + avgError
//        + ", pM=" + probM
//        + ", pU=" + probU
//        + ", wM=" + weightM
//        + ", wU=" + weightU
//      );

      Result result = new Result();
      result.setParam(HEADER_ATTRIBUTE, attributeName);
      result.addMetric("Error", BigDecimal.valueOf(avgError));
      result.addMetric("Avg. frequency", BigDecimal.valueOf(optionalAvgFrequency.getAsDouble()));
      result.addMetric("m-prob", BigDecimal.valueOf(probM));
      result.addMetric("u-prob", BigDecimal.valueOf(probU));
      result.addMetric(M_WEIGHT, BigDecimal.valueOf(weightM));
      result.addMetric(U_WEIGHT, BigDecimal.valueOf(weightU));
      result.addMetric(WEIGHT, BigDecimal.valueOf(WeightUtils.getWeightDiff(probM, probU)));
      resultSet.addResult(result);
//      Table summary =
//        attributeFrequencies.get(attributeName).summarize(HEADER_RELATIVE_FREQUENCY, AggregateFunctions
//        .mean)
//          .apply();
//      System.out.println(summary.printAll());
    }
    addNormalizedWeights(resultSet);
    return resultSet;
  }

  private void addNormalizedWeights(ResultSet resultSet) {
    Table resultSetAsTable = resultSet.getAsTable();
    BigDecimal maxMWeight =
      BigDecimal.valueOf(AggregateFunctions.max.summarize(resultSetAsTable.doubleColumn(M_WEIGHT)));
    BigDecimal minUWeight =
      BigDecimal.valueOf(AggregateFunctions.min.summarize(resultSetAsTable.doubleColumn(U_WEIGHT)));
    for (Result result : resultSet.getResults()) {
      BigDecimal normalizedMWeight = result.getMetrics().get(M_WEIGHT).divide(maxMWeight, 4,
        RoundingMode.HALF_UP
      );
      BigDecimal normalizedUWeight = result.getMetrics().get(U_WEIGHT).divide(minUWeight, 4,
        RoundingMode.HALF_UP
      ).multiply(BigDecimal.valueOf(-1));
      result.addMetric(M_WEIGHT + NORM_POSTFIX, normalizedMWeight);
      result.addMetric(U_WEIGHT + NORM_POSTFIX, normalizedUWeight);
      result.addMetric(WEIGHT + NORM_POSTFIX, normalizedMWeight.subtract(normalizedUWeight));
    }
  }

  protected String buildDescription() {
    return "Properties related to the discriminatory power of attributes and possible weights";
  }
}

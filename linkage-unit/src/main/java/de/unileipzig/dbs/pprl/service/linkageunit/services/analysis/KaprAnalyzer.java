package de.unileipzig.dbs.pprl.service.linkageunit.services.analysis;

import de.unileipzig.dbs.pprl.core.analyzer.DataSetAnalyzer;
import de.unileipzig.dbs.pprl.core.analyzer.attribute.AttributeAvailability;
import de.unileipzig.dbs.pprl.core.analyzer.linking.LinkAnalyzer;
import de.unileipzig.dbs.pprl.core.analyzer.results.Result;
import de.unileipzig.dbs.pprl.core.analyzer.results.ResultSet;
import de.unileipzig.dbs.pprl.core.common.RecordUtils;
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.impl.AttributeLight;
import de.unileipzig.dbs.pprl.core.common.model.impl.PersonalAttributeType;
import de.unileipzig.dbs.pprl.core.matcher.model.api.LinkageProcessDataSet;
import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoRecordPair;
import org.apache.commons.lang3.StringUtils;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.LongColumn;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.unileipzig.dbs.pprl.service.linkageunit.services.LinkImprovementService.PROPERTY_LINK_FROM_UPPER_LAYER;

public class KaprAnalyzer extends LinkAnalyzer {

  public static final String NAME = "Privacy Measure KAPR";
  public static final String INDIVIDUAL_KAPR_SCORES = "Individual KAPR scores";
  public static final String KEY_SIMULATE = "SIMULATE";
  public static final String KEY_MIN_SIM = "MIN_SIM";
  public static final String KEY_MAX_SIM = "MAX_SIM";
  public static final String EXCLUDE_AVAILABILITY = "EXCLUDE_AVAILABILITY";
  final private Collection<Record> referenceDataset;
  final private Map<String, String> parameters;

//  private final double defaultMinSimilarity = 0.4;
//  private final double defaultMaxSimilarity = 1.01;

  public KaprAnalyzer(Map<String, String> parameters, Collection<Record> referenceDataset) {
    this.parameters = parameters == null ? new HashMap<>() : parameters;
    this.referenceDataset = referenceDataset;
  }

  @Override
  public ResultSet analyze(Collection<RecordPair> pairs) {
    ResultSet resultSet = new ResultSet(NAME);
//    double minSimilarity = defaultMinSimilarity;
//    double maxSimilarity = defaultMaxSimilarity;
//    try {
//      minSimilarity = Double.parseDouble(parameters.get(KEY_MIN_SIM));
//    } catch (Exception e) {
//    }
//    try {
//      maxSimilarity = Double.parseDouble(parameters.get(KEY_MAX_SIM));
//    } catch (Exception e) {
//    }

    List<Double> minSimilarities = new ArrayList<>();
    minSimilarities.add(0.0);
    minSimilarities.add(0.2);
    minSimilarities.add(0.4);
//    minSimilarities.add(0.6);
//    minSimilarities.add(0.8);
//    minSimilarities.add(minSimilarity);

    List<Double> maxSimilarities = List.of(1.0, 1.01);
//    List<Double> maxSimilarities = List.of(1.0);

    List<String> attributeNames = RecordUtils.getAttributeNames(referenceDataset).stream()
      .sorted(new PersonalAttributeType.AttributeNameComparator()).toList();

    addToResultSet(resultSet, attributeNames,
      pairs.stream()
        .map(p -> (MongoRecordPair) p)
        .filter(p -> !p.getProperties().contains(PROPERTY_LINK_FROM_UPPER_LAYER))
        .filter(p -> !p.getProperties().contains(LinkageProcessDataSet.REPLACED))
        .map(mp -> (RecordPair) mp)
        .toList(),
      "DEFAULT"
    );

    if (parameters.containsKey(KEY_SIMULATE)) {
      for (Double currentMinSimilarity : minSimilarities) {
        for (Double currentMaxSimilarity : maxSimilarities) {
          if (currentMaxSimilarity > 1.001 && currentMinSimilarity > 0.001) {
            continue;
          }
          addToResultSet(resultSet, attributeNames,
            simulateAttributeSelection(referenceDataset, pairs, currentMinSimilarity,
              currentMaxSimilarity
            ),
            "sim=(" + currentMinSimilarity + "," + currentMaxSimilarity + ")"
          );
        }
      }
    }
    return resultSet;
  }

  private void addToResultSet(ResultSet resultSet, List<String> attributeNames, Collection<RecordPair> pairs,
    String methodName) {
    double sum = 0.0;

    Table kaprResults = Table.create(
      INDIVIDUAL_KAPR_SCORES + " (" + methodName + ")",
      LongColumn.create("kSetSize"),
      LongColumn.create("numAttr"),
      DoubleColumn.create("pSum"),
      DoubleColumn.create("K"),
      StringColumn.create("rID")
    );
    attributeNames.forEach(an -> kaprResults.addColumns(StringColumn.create(an)));
    kaprResults.addColumns(StringColumn.create("attrSims"));
    int pairCount = 0;
    for (RecordPair pair : pairs) {
      int rCount = 0;
      for (Record record : pair.getRecords()) {
        String rId = pair.getPairId() + "-" + record.getId().getUniqueId();
        long k = determineAnonymitySetSize(record);
        double pSum = determineSumOfDisclosedProportions(record);
        long d = record.getAttributes().size();
        double partialK = pSum / k;
        sum += partialK;
        Row row = kaprResults.appendRow();
        row.setLong(0, k);
        row.setLong(1, d);
        row.setDouble(2, pSum);
        row.setDouble(3, partialK);
        row.setString(4, rId);
        attributeNames.forEach(an -> row.setString(
          an,
          record.getAttribute(an).orElse(new AttributeLight("")).getAsString()
        ));
        row.setString("attrSims", StringUtils.join(pair.getAttributeSimilarities().get()));
        rCount++;
      }
      pairCount++;
    }
    resultSet.addAdditionalResult(kaprResults);

//    System.out.println(kaprResults.print(20));
    long N = kaprResults.rowCount();
    long D = Math.max(1, attributeNames.size());
    double kapr = sum / (N * D);
    Result result = new Result();
    if (Double.isNaN(kapr)) {
      result.addMetric("KAPR", BigDecimal.valueOf(-1)); // Or handle the case with a default value
    } else {
      result.addMetric("KAPR", BigDecimal.valueOf(kapr));
    }
    result.setParam("METHOD", methodName);

    String excludeAvailability = parameters.get(EXCLUDE_AVAILABILITY);
    if (!Boolean.parseBoolean(excludeAvailability)) {
      List<Record> records =
        pairs.parallelStream().flatMap(pair -> pair.getRecords().stream()).collect(Collectors.toList());
      ResultSet attributeAvailability =
        DataSetAnalyzer.runAttributeAnalyzers(records, List.of(new AttributeAvailability())).getFirst();
      for (String attributeName : attributeNames) {
        BigDecimal availability = BigDecimal.ZERO;
        for (Result availabilityResult : attributeAvailability.getResults()) {
          if (availabilityResult.getParams().get(HEADER_ATTRIBUTE).equals(attributeName)) {
            availability = availabilityResult.getMetrics().get(AttributeAvailability.VALID);
          }
        }
        result.addMetric(attributeName + " availability", availability);
      }
    }
    resultSet.addResult(result);
  }

  private double determineSumOfDisclosedProportions(Record record) {
    return record.getAttributes().size();
  }

  private long determineAnonymitySetSize(Record record) {
    long count = referenceDataset.stream()
      .filter(ref -> {
        for (String attrName : record.getAttributes().keySet()) {
          Attribute attr = record.getAttribute(attrName).get();
          Optional<Attribute> refAttr = ref.getAttribute(attrName);
          if (refAttr.isPresent()) {
            if (!attr.getAsString().equals(refAttr.get().getAsString())) {
              return false;
            }
          }
        }
        return true;
      })
      .count();
    if (count == 0) {
      logger.error("Record should be at least once in the reference dataset." +
        "Setting it to 1 as a dirty fix.");
      return 1;
    }
    return count;
  }

  //  private Record
  public static Collection<RecordPair> simulateAttributeSelection(Collection<Record> plainTextRecords,
    Collection<RecordPair> linkedPairs, double minSimilarity, double maxSimilarity) {
    Map<String, MongoRecordPair> pairsWithAttributeSimilarities = linkedPairs.stream()
      .map(p -> (MongoRecordPair) p)
      .filter(p -> p.getProperties().contains(PROPERTY_LINK_FROM_UPPER_LAYER))
      .collect(Collectors.toMap(RecordPair::getPairId, p -> p));
    Map<String, Record> recordsById = plainTextRecords.stream()
      .collect(Collectors.toMap(record -> record.getId().getUniqueLikeId(), r -> r));
    return linkedPairs.stream()
      .map(p -> (MongoRecordPair) p)
      .filter(p -> !p.getProperties().contains(PROPERTY_LINK_FROM_UPPER_LAYER))
      .filter(p -> !p.getProperties().contains(LinkageProcessDataSet.REPLACED))
      .map(p -> {
        MongoRecordPair out = p.duplicate();
        out.getLeftRecord().removeAllAttributes();
        out.getRightRecord().removeAllAttributes();
        out.setAttributeSimilarities(
          pairsWithAttributeSimilarities.get(out.getPairId()).getAttributeSimilarities()
            .get());
        Map<String, Double> attrSims = out.getAttributeSimilarities().get();
        for (String attrName : attrSims.keySet()) {
          Double sim = attrSims.get(attrName);
          if (sim < maxSimilarity && sim > minSimilarity) {
            Record leftRecord = recordsById.get(p.getLeftRecord().getId().getUniqueLikeId());
            leftRecord.getAttribute(attrName).ifPresent(a -> out.getLeftRecord().setAttribute(attrName, a));
            Record rightRecord = recordsById.get(p.getRightRecord().getId().getUniqueLikeId());
            rightRecord.getAttribute(attrName).ifPresent(a -> out.getRightRecord().setAttribute(attrName, a));
          } else {
            sim = sim;
          }
        }
        return out;
      })
      .map(mrp -> (RecordPair) mrp)
      .toList();
  }
}

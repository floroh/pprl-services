package de.unileipzig.dbs.pprl.core.analyzer.cluster;

import de.unileipzig.dbs.pprl.core.analyzer.cluster.data.AttributePairDiff;
import de.unileipzig.dbs.pprl.core.analyzer.cluster.data.Pair;
import de.unileipzig.dbs.pprl.core.analyzer.results.Result;
import de.unileipzig.dbs.pprl.core.analyzer.results.ResultSet;
import de.unileipzig.dbs.pprl.core.common.HelperUtils;
import de.unileipzig.dbs.pprl.core.common.comparators.ComposedIdComparator;
import de.unileipzig.dbs.pprl.core.common.comparators.RecordIdComparator;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordId;
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

/**
 * Analyze the cluster of records belonging to the same real world entity
 * by the patterns of differences between pairs of these records.
 */
public class ClusterPairwiseDiffPattern extends ClusterPairwiseDiff {

  public static final String HEADER_SCHEMA = "SCHEMA";
  public static final double VERY_SIMILAR_BOUNDARY = 0.8;
  public static final double DISSIMILAR_BOUNDARY = 0.2;

  public static final double MIN_PATTERN_FREQUENCY = 100;

  public ResultSet analyze(Map<String, List<Record>> clusters) {
    ResultSet resultSet = getResultSet();
    resultSet.setDescription(buildDescription());

    Map<String, Long> schemaCounts = new HashMap<>();
    Map<String, List<Pair<RecordId>>> recordPairIdsBySchema = new HashMap<>();

    for (List<Record> cluster : clusters.values()) {
      List<Pair<Record>> recordPairs = buildRecordPairs(cluster);
      for (Pair<Record> recordPair : recordPairs) {
        String schemaString = constructSchemaString(recordPair);
        if (!schemaCounts.containsKey(schemaString)) {
          schemaCounts.put(schemaString, 1L);
        } else {
          schemaCounts.put(schemaString, schemaCounts.get(schemaString) + 1);
        }
        if (!recordPairIdsBySchema.containsKey(schemaString)) {
          recordPairIdsBySchema.put(schemaString, new ArrayList<>());
        }
        recordPairIdsBySchema.get(schemaString).add(
          new Pair<>(recordPair.getV0().getId(), recordPair.getV1().getId())
        );
      }
    }
    Table schemaFrequencyTable = buildFrequencyResult(schemaCounts);
    resultSet.addAdditionalResult(schemaFrequencyTable);
    schemaFrequencyTable.first(10).stream()
      .forEach(row -> resultSet.addResult(convertResultRow(row)));
    recordPairIdsBySchema.forEach((schema, idPairs) -> {
      Table pairTable = tableFromPairs(idPairs).setName(schema);
      if (pairTable.rowCount() > MIN_PATTERN_FREQUENCY) {
        resultSet.addAdditionalResult(pairTable);
      }
    });
    return resultSet;
  }

  private String constructSchemaString(Pair<Record> recordPair) {
    StringBuilder schema = new StringBuilder();
    // TODO Use RecordUtils.getAttributeNames to get all names because so far both missing is not working
    for (AttributePairDiff apd : buildAttributePairDiffs(recordPair)) {
      String attributeName = apd.getAttributeName();
      String attrSchemaMarker = determineAttributeSchemaMarker(apd);
      schema.append("_").append(attributeName);
      schema.append(":").append(attrSchemaMarker);
    }
    String schemaString = schema.toString();
    return schemaString;
  }

  private String determineAttributeSchemaMarker(AttributePairDiff apd) {
    String attrSchemaMarker = "ss";
    if (getEqualityBasedDistance(apd) == 0) {
      attrSchemaMarker = "==";
    } else {
      boolean leftEmpty = apd.getV0().isEmpty();
      boolean rightEmpty = apd.getV1().isEmpty();
      if (leftEmpty && rightEmpty) {
        attrSchemaMarker = "??";
      } else if (leftEmpty || rightEmpty) {
        attrSchemaMarker = "?.";
      } else {
        double sim = apd.getSimilarity();
        if (sim > VERY_SIMILAR_BOUNDARY) {
          attrSchemaMarker = "SS";
        } else if (sim < DISSIMILAR_BOUNDARY) {
          attrSchemaMarker = "DD";
        }
      }
    }
    return attrSchemaMarker;
  }

  protected Table buildFrequencyResult(Map<String, Long> schemaCounts) {
    StringColumn colValue = StringColumn.create(HEADER_SCHEMA);
    LongColumn colAbsFrequency = LongColumn.create(HEADER_ABSOLUTE_FREQUENCY);
    DoubleColumn colRelFrequency = DoubleColumn.create(HEADER_RELATIVE_FREQUENCY);
    long numberOfValues = calculateTotal(schemaCounts);
    HelperUtils.reverseSortByValue(schemaCounts).forEach((schema, count) -> {
      colValue.append(schema);
      colAbsFrequency.append(count);
      double relativeFrequency = (double) count / numberOfValues;
      colRelFrequency.append(relativeFrequency);
    });
    return Table.create("SchemaFrequency", colValue, colAbsFrequency, colRelFrequency);
  }

  private Result convertResultRow(Row row) {
    Result result = new Result();
    result.setParam("Schema", row.getString(HEADER_SCHEMA));
    result.addMetric(
      HEADER_ABSOLUTE_FREQUENCY,
      BigDecimal.valueOf(row.getLong(HEADER_ABSOLUTE_FREQUENCY))
    );
    result.addMetric(
      HEADER_RELATIVE_FREQUENCY,
      BigDecimal.valueOf(row.getDouble(HEADER_RELATIVE_FREQUENCY))
    );
    return result;
  }

  public static long calculateTotal(Map<String, Long> frequencies) {
    return frequencies.values().stream().mapToLong(l -> l).sum();
  }

  public static Table tableFromPairs(Collection<Pair<RecordId>> idPairs) {
    StringColumn colId0 = StringColumn.create(LEFT_ID);
    StringColumn colId1 = StringColumn.create(RIGHT_ID);
    idPairs.forEach(pair -> {
      List<RecordId> idList = new ArrayList<>();
      idList.add(pair.getV0());
      idList.add(pair.getV1());
      idList.sort(new RecordIdComparator());
      colId0.append(idList.get(0).getUniqueId());
      colId1.append(idList.get(1).getUniqueId());
//      List<String> idList = new ArrayList<>();
//      idList.add(pair.getV0().getUniqueId());
//      idList.add(pair.getV1().getUniqueId());
////      idList.sort(new ComposedIdComparator());
//      colId0.append(idList.get(0));
//      colId1.append(idList.get(1));
    });
    return Table.create("Links", colId0, colId1);
  }

  private String buildDescription() {
    return
      "The attribute values of the same type are compared for each record pair within each cluster.\n" +
        "These attribute pair similarities are combined to construct record difference patterns.\n" +
        "(==) equal, (SS) very similar (sim > " + VERY_SIMILAR_BOUNDARY + ")," +
        " (ss) somewhat similar, (DD) dissimlar (sim < " + DISSIMILAR_BOUNDARY + ")\n" +
        "(??) both missing, (?.) one missing";
  }

  @Override
  public String toString() {
    return "ClusterPairwiseDiffPattern";
  }
}

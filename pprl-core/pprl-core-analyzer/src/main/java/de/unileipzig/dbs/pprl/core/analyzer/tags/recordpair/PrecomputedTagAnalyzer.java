package de.unileipzig.dbs.pprl.core.analyzer.tags.recordpair;

import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import de.unileipzig.dbs.pprl.core.common.monitoring.Tag;
import de.unileipzig.dbs.pprl.core.common.monitoring.TagTable;
import de.unileipzig.dbs.pprl.core.matcher.evaluation.GroundTruth;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

import java.util.*;

public class PrecomputedTagAnalyzer implements RecordPairAnalyzer {
  public static final String ID_SEPARATOR = "#";

  private Map<String, List<Tag>> tags;

  public PrecomputedTagAnalyzer(Map<String, List<Tag>> tags) {
    this.tags = tags;
  }

  @Override
  public List<Tag> getTags(RecordPair recordPair) {
    String leftId = recordPair.getLeftRecord().getId().getUniqueId();
    String rightId = recordPair.getRightRecord().getId().getUniqueId();
    String key = buildKey(leftId, rightId);
    return tags.getOrDefault(key, Collections.emptyList());
  }

  public static PrecomputedTagAnalyzer createFromTable(Table tagTable) {
    Map<String, List<Tag>> tags = new HashMap<>();
    tagTable.splitOn(GroundTruth.LEFT_ID, GroundTruth.RIGHT_ID).getSlices()
      .forEach(slice -> {
        Row firstRow = slice.asTable().row(0);
        String id0 = firstRow.getString(GroundTruth.LEFT_ID);
        String id1 = firstRow.getString(GroundTruth.RIGHT_ID);
        List<Tag> curTags = new ArrayList<>();
        slice.forEach(r -> {
          String attribute = r.getString(TagTable.ATTRIBUTE);
          String tag = r.getString(TagTable.TAG);
          String stringValue = r.getString(TagTable.TAG_STRING);
          Double numericValue = r.getDouble(TagTable.TAG_NUMERIC);
          curTags.add(new Tag(id0, id1, attribute, tag, stringValue, numericValue));
        });
        tags.put(buildKey(id0, id1), curTags);
      });
    return new PrecomputedTagAnalyzer(tags);
  }

  private static String buildKey(String id0, String id1) {
    return id0 + ID_SEPARATOR + id1;
  }
}

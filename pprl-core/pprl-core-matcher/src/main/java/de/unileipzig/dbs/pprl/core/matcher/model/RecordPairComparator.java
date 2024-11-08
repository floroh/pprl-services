package de.unileipzig.dbs.pprl.core.matcher.model;

import de.unileipzig.dbs.pprl.core.common.comparators.RecordComparator;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;

import java.util.Comparator;

public class RecordPairComparator {

  public static final Comparator<RecordPair> BY_SIMILARITY = new RecordPairBySimilarityComparator();
  public static final Comparator<RecordPair> BY_IDS = new RecordPairByIdsComparator();

  private static class RecordPairBySimilarityComparator implements Comparator<RecordPair> {
    @Override
    public int compare(RecordPair o1, RecordPair o2) {
      return Double.compare(o1.getSimilarity(), o2.getSimilarity()) * -1;
    }
  }

  private static class RecordPairByIdsComparator implements Comparator<RecordPair> {
    private RecordComparator comparator = new RecordComparator();

    public int compare(RecordPair pair0, RecordPair pair1) {
      int ret = comparator.compare(
        pair0.getLeftRecord(),
        pair1.getLeftRecord()
      );
      if (ret == 0) {
        ret = comparator.compare(
          pair0.getRightRecord(),
          pair1.getRightRecord()
        );
      }
      return ret;
    }
  }
}

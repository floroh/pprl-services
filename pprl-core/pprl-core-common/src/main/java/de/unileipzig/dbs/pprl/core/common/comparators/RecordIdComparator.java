package de.unileipzig.dbs.pprl.core.common.comparators;

import de.unileipzig.dbs.pprl.core.common.model.api.RecordId;
import se.sawano.java.text.AlphanumericComparator;

import java.util.Comparator;
import java.util.Locale;

public class RecordIdComparator implements Comparator<RecordId> {
  private static final SourceIdComparator srcComparator = new SourceIdComparator();
  private static final Comparator<CharSequence> naturalComparator = new AlphanumericComparator(Locale.ENGLISH);

  private boolean sourceFirst = false;

  public RecordIdComparator(boolean sourceFirst) {
    this.sourceFirst = sourceFirst;
  }

  public RecordIdComparator() {
  }

  public int compare(RecordId id0, RecordId id1) {
//    if (id0.equals(id1)) {
//      return 0;
//    }
    int ret = 0;
    if (sourceFirst) {
      ret = srcComparator.compare(id0.getSourceId(), id1.getSourceId());
      if (ret == 0) {
        ret = naturalComparator.compare(id0.getLocalId(), id1.getLocalId());
      }
    } else {
      ret = naturalComparator.compare(id0.getLocalId(), id1.getLocalId());
      if (ret == 0) {
        ret = srcComparator.compare(id0.getSourceId(), id1.getSourceId());
      }
    }
    return ret;
  }
}

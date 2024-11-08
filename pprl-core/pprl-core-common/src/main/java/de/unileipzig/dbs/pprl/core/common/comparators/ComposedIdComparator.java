package de.unileipzig.dbs.pprl.core.common.comparators;

import de.unileipzig.dbs.pprl.core.common.model.api.RecordId;
import de.unileipzig.dbs.pprl.core.common.model.impl.RecordIdComposed;

import java.util.Comparator;

public class ComposedIdComparator implements Comparator<String> {
  private RecordIdComparator comparator = new RecordIdComparator();

  public int compare(String id0, String id1) {
    RecordId rid0 = RecordIdComposed.ofComposed(id0);
    RecordId rid1 = RecordIdComposed.ofComposed(id1);

    return comparator.compare(rid0, rid1);
  }
}

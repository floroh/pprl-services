package de.unileipzig.dbs.pprl.core.common.comparators;

import java.util.Comparator;

public class SourceIdComparator implements Comparator<String> {
  public int compare(String src0, String src1) {
    if (src0.equals("dup") && src1.equals("org")) {
      return 1;
    } else if (src0.equals("org") && src1.equals("dup")) {
      return -1;
    }
    return src0.compareTo(src1);
  }
}

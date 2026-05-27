package de.unileipzig.dbs.pprl.core.analyzer.cluster.data;

import org.apache.commons.lang3.StringUtils;

public class CustomDiff {
  private CustomOperation operation;
  private String s0;
  private String s1;

  CustomDiff(CustomOperation operation, String s0, String s1) {
    this.operation = operation;
    this.s0 = s0;
    this.s1 = s1;
  }

  public String toString() {
    return switch (operation) {
      case EQUAL -> StringUtils.repeat("?", s0.length());
      case INSERT -> wrap(s0);
      case SWAP -> wrap(s0 + "<>" + s1);
      case REPLACEMENT -> wrap(s0 + "/" + s1);
      default -> "INVALIDOP";
    };
  }

  public CustomOperation getOperation() {
    return operation;
  }

  public String getS0() {
    return s0;
  }

  public String getS1() {
    return s1;
  }

  private String wrap(String in) {
    return "[" + in + "]";
  }
}

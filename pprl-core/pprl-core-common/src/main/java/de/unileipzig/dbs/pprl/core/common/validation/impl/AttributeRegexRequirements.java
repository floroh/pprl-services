package de.unileipzig.dbs.pprl.core.common.validation.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AttributeRegexRequirements {

  public static final String INCLUDES_SPACE = "INCLUDES_SPACE";
  public static final String INCLUDES_HYPHEN = "INCLUDES_HYPHEN";
  public static final String IS_EMPTY_PLACEHOLDER = "IS_EMPTY_PLACEHOLDER";
  public static final Map<String, List<String>> PRECONFIGURED = Map.ofEntries(
    Map.entry(INCLUDES_SPACE, List.of(".+ .+")),
    Map.entry(INCLUDES_HYPHEN, List.of(".+-.+")),
    Map.entry(IS_EMPTY_PLACEHOLDER, List.of("null", "NULL", "Null", "NaN"))
  );

  private final List<String> validRegex;

  private final List<String> invalidRegex;

  private final List<String> reportRegex;

  public AttributeRegexRequirements(List<String> validRegex, List<String> invalidRegex,
    List<String> reportRegex) {
    this.validRegex = validRegex;
    this.invalidRegex = invalidRegex;
    this.reportRegex = reportRegex;
  }

  public AttributeRegexRequirements() {
    validRegex = new ArrayList<>();
    invalidRegex = new ArrayList<>();
    reportRegex = new ArrayList<>();
  }

  public List<String> getValidRegex() {
    return validRegex;
  }

  public List<String> getInvalidRegex() {
    return invalidRegex;
  }

  public List<String> getReportRegex() {
    return reportRegex;
  }

  public void addValidRegex(List<String> expressions) {
    addIfNew(validRegex, expressions);
  }

  public void addInvalidRegex(List<String> expressions) {
    addIfNew(invalidRegex, expressions);
  }

  public void addReportRegex(List<String> expressions) {
    addIfNew(reportRegex, expressions);
  }

  private void addIfNew(List<String> oldExpressions, List<String> newExpressions) {
    for (String s : newExpressions) {
      if (oldExpressions.contains(s)) continue;
      oldExpressions.add(s);
    }
  }
}

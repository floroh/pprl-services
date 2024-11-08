package de.unileipzig.dbs.pprl.core.matcher.linking;

import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import de.unileipzig.dbs.pprl.core.common.model.impl.MatchGrade;

public class MatchGradeBasedTester implements RecordPairTester {

  public static final MatchGrade DEFAULT_MINIMAL_MATCH_GRADE = MatchGrade.PROBABLE_MATCH;

  private MatchGrade minimalMatchGrade = DEFAULT_MINIMAL_MATCH_GRADE;

  public MatchGradeBasedTester(MatchGrade minimalMatchGrade) {
    this.minimalMatchGrade = minimalMatchGrade;
  }

  private MatchGradeBasedTester() {
  }

  @Override
  public boolean test(RecordPair recordPair) {
    return recordPair.getClassification().isAtLeast(minimalMatchGrade);
  }

  public MatchGrade getMinimalMatchGrade() {
    return minimalMatchGrade;
  }

  public void setMinimalMatchGrade(MatchGrade minimalMatchGrade) {
    this.minimalMatchGrade = minimalMatchGrade;
  }
}

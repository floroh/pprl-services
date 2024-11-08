package de.unileipzig.dbs.pprl.core.analyzer.cluster.data;

import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.matcher.similarityCalculation.attribute.StringSimilarityCalculator;
import name.fraser.neil.plaintext.diff_match_patch;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class AttributePairDiff extends AttributePair {
  private List<CustomDiff> diffs;
  private double equalCharactersShare = 1.0;

  private static StringSimilarityCalculator comparator =
    new StringSimilarityCalculator(StringSimilarityCalculator.SimilarityMethod.LEVENSHTEIN);

  public AttributePairDiff(AttributePair ap) {
    this(ap.getAttributeName(), ap.getV0(), ap.getV1());
  }

  AttributePairDiff(String attributeName, Attribute attr0, Attribute attr1) {
    super(attributeName, attr0, attr1);
  }

  public double getDistance() {
    return 1 - getSimilarity();
  }

  public double getSimilarity() {
    return comparator.calculateSimilarity(v0, v1);
  }

  public List<CustomDiff> getDiffs() {
    String s0 = v0.getAsString();
    String s1 = v1.getAsString();

    int equalChars = 0;
    diffs = new ArrayList<>();
    LinkedList<diff_match_patch.Diff> tmpDiffs = new diff_match_patch().diff_main(s0, s1);
    for (int i = 0; i < tmpDiffs.size(); i++) {
      diff_match_patch.Diff curDiff = tmpDiffs.get(i);
      switch (curDiff.operation) {
        case EQUAL:
          equalChars += curDiff.text.length();
          diffs.add(
            new CustomDiff(CustomOperation.EQUAL, curDiff.text, ""));
          break;
        case DELETE:
          if (tmpDiffs.size() > i + 1) {
            diff_match_patch.Diff nextDiff = tmpDiffs.get(i + 1);
            if (nextDiff.operation == diff_match_patch.Operation.INSERT) {
              diffs.add(new CustomDiff(CustomOperation.REPLACEMENT,
                curDiff.text, nextDiff.text
              ));
              i += 1;
              break;
            }
            if (tmpDiffs.size() > i + 2) {
              diff_match_patch.Diff afterNextDiff = tmpDiffs.get(i + 2);
              if (nextDiff.operation == diff_match_patch.Operation.EQUAL &&
                afterNextDiff.operation == diff_match_patch.Operation.INSERT &&
                afterNextDiff.text.equals(curDiff.text)) {
                diffs.add(
                  new CustomDiff(CustomOperation.SWAP, curDiff.text,
                    nextDiff.text
                  ));
                i += 2;
                break;
              }
            }
          }
        case INSERT:
          diffs.add(
            new CustomDiff(CustomOperation.INSERT, curDiff.text, ""));
      }
    }
    equalCharactersShare = (double) equalChars / Math.min(s0.length(), s1.length());
    return diffs;
  }

  public double getEqualCharactersShare() {
    if (needsDiffEvaluation()) {
      getDiffs();
    }
    return equalCharactersShare;
  }

  private boolean needsDiffEvaluation() {
    return (diffs == null);
  }

  public static void setComparator(StringSimilarityCalculator newComparator) {
    comparator = newComparator;
  }
}

package de.unileipzig.dbs.pprl.core.matcher;

import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import de.unileipzig.dbs.pprl.core.common.model.impl.MatchGrade;
import de.unileipzig.dbs.pprl.core.common.monitoring.Tag;
import de.unileipzig.dbs.pprl.core.matcher.classification.Classifier;

import java.util.Optional;

public class MatcherUtils {

  public static RecordPair addLabelBasedOnMatchGrade(RecordPair rp) {
    return rp.addTag((rp.getClassification().isAtLeast(MatchGrade.PROBABLE_MATCH)
      ? Classifier.Label.TRUE_MATCH : Classifier.Label.TRUE_NON_MATCH).toString());
  }

  public static Optional<Classifier.Label> getLabel(RecordPair rp) {
    Optional<Tag> targetLabelTag = rp.getTags().stream()
      .filter(tag -> tag.getTag().equals(Classifier.Label.TRUE_MATCH.name()) ||
        tag.getTag().equals(Classifier.Label.TRUE_NON_MATCH.name()))
      .findAny();
    return targetLabelTag.map(tag -> Classifier.Label.valueOf(tag.getTag()));
  }
}

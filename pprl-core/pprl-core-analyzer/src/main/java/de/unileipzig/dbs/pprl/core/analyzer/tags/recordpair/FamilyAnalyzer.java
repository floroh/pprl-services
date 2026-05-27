package de.unileipzig.dbs.pprl.core.analyzer.tags.recordpair;

import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import de.unileipzig.dbs.pprl.core.common.model.impl.PersonalAttributeType;
import de.unileipzig.dbs.pprl.core.common.monitoring.Tag;
import de.unileipzig.dbs.pprl.core.matcher.TagUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FamilyAnalyzer implements RecordPairAnalyzer {

  public static final String FAMILY = "ALL_FAMILY";

  @Override
  public List<Tag> getTags(RecordPair recordPair) {
    List<Tag> tags = new ArrayList<>();
    if (equalAttribute(recordPair, PersonalAttributeType.LASTNAME.name())
      && equalAttribute(recordPair, PersonalAttributeType.ADDRESS.name())
      && !equalAttribute(recordPair, PersonalAttributeType.FIRSTNAME.name())) {
      tags.add(TagUtils.create(recordPair, null, FAMILY, null, null));
    }
    return tags;
  }

  private boolean equalAttribute(RecordPair pair, String attributeName) {
    Record leftRecord = pair.getLeftRecord();
    Record rightRecord = pair.getRightRecord();
    Optional<Attribute> leftAttribute = leftRecord.getAttribute(attributeName);
    Optional<Attribute> rightAttribute = rightRecord.getAttribute(attributeName);
    if (leftAttribute.isPresent() && rightAttribute.isPresent()
      && leftAttribute.get().equals(rightAttribute.get())) {
      return true;
    }
    return false;
  }
}

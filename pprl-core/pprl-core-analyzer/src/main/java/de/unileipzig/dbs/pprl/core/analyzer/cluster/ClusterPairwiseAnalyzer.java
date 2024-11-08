package de.unileipzig.dbs.pprl.core.analyzer.cluster;

import de.unileipzig.dbs.pprl.core.analyzer.attribute.AttributeAvailability;
import de.unileipzig.dbs.pprl.core.analyzer.cluster.data.AttributePair;
import de.unileipzig.dbs.pprl.core.analyzer.cluster.data.Pair;
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.impl.PersonalAttributeType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class ClusterPairwiseAnalyzer extends ClusterAnalyzer {

  protected List<Pair<Record>> buildRecordPairs(List<Record> records) {
    List<Pair<Record>> recordPairs = new ArrayList<>();
    for (int i = 0; i < records.size(); i++) {
      for (int j = i + 1; j < records.size(); j++) {
        recordPairs.add(new Pair<>(records.get(i), records.get(j)));
      }
    }
    return recordPairs;
  }

  protected List<AttributePair> buildAttributePairs(
    Pair<Record> recordPair) {
    List<AttributePair> attributePairs = new ArrayList<>();

    Record r0 = recordPair.getV0();
    Record r1 = recordPair.getV1();

    List<String> matchingAttributeNames = getSortedAttributeNames(new Pair<>(r0, r1));

    for (String attributeName : matchingAttributeNames) {
      Attribute attr0 = r0.getAttribute(attributeName).get();
      Attribute attr1 = r1.getAttribute(attributeName).get();
      if (!AttributeAvailability.isInvalidOrEmpty(attributeName, attr0) ||
        !AttributeAvailability.isInvalidOrEmpty(attributeName, attr1)) {
        attributePairs.add(new AttributePair(attributeName, attr0, attr1));
      }
    }
    return attributePairs;
  }

  protected List<String> getSortedAttributeNames(Pair<Record> recordPair) {
    Set<String> attributeNames = new HashSet<>();
    for (String attrName0 : recordPair.getV0().getAttributeNames()) {
      Optional<Attribute> optionalName1 = recordPair.getV1().getAttribute(attrName0);
      if (optionalName1.isPresent()) {
        attributeNames.add(attrName0);
      }
    }
    return sort(attributeNames);
  }

  public List<String> sort(Collection<String> attributeNames) {
    return attributeNames.stream()
      .sorted(new PersonalAttributeType.AttributeNameComparator())
      .collect(Collectors.toList());
  }

  protected double getEqualityBasedDistance(AttributePair ap) {
    Object s0 = ap.getV0().getObject();
    Object s1 = ap.getV1().getObject();
    return s0.equals(s1) ? 0 : 1;
  }

}

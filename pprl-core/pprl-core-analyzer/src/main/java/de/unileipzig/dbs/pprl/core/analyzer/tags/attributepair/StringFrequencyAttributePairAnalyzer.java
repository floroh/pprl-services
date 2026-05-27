package de.unileipzig.dbs.pprl.core.analyzer.tags.attributepair;//package de.unileipzig.dbs.pprl.service.protocol.analyzer.attributepair;
//
//import de.unileipzig.dbs.pprl.core.common.monitoring.Tag;
//import de.unileipzig.dbs.pprl.service.protocol.analyzer.attribute.StringFrequencyAnalyzer;
//
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//import static de.unileipzig.dbs.pprl.service.protocol.analyzer.attribute.StringFrequencyAnalyzer.UNKNOWN;
//
//public class StringFrequencyAttributePairAnalyzer implements AttributePairAnalyzer<String> {
//  public static final String ONE_UNKNOWN = UNKNOWN + "_ONE";
//  public static final String BOTH_UNKNOWN = UNKNOWN + "_BOTH";
//  public static final String NONE_UNKNOWN = UNKNOWN + "_NONE";
//
//  private StringFrequencyAnalyzer stringFrequencyAnalyzer;
//
//  public StringFrequencyAttributePairAnalyzer(
//    StringFrequencyAnalyzer stringFrequencyAnalyzer) {
//    this.stringFrequencyAnalyzer = stringFrequencyAnalyzer;
//  }
//
//  @Override
//  public List<Tag> getTags(String attrName, String attr0, String attr1) {
//    final List<Tag> tags = new ArrayList<>();
//
//    List<String> leftTags = stringFrequencyAnalyzer.getTags(attrName, attr0);
//    List<String> rightTags = stringFrequencyAnalyzer.getTags(attrName, attr1);
//    boolean leftIsUnknown = isUnknown(leftTags);
//    boolean rightIsUnknown = isUnknown(rightTags);
//    if (leftIsUnknown && !rightIsUnknown) {
////      tags.addAll(leftTags);
//      tags.addAll(rightTags);
//      tags.add(attrName + ONE_UNKNOWN);
//    } else if (!leftIsUnknown && rightIsUnknown) {
//      tags.addAll(leftTags);
////      tags.addAll(rightTags);
//      tags.add(attrName + ONE_UNKNOWN);
//    } else if (leftIsUnknown && rightIsUnknown) {
//      tags.add(attrName + BOTH_UNKNOWN);
//    } else {
//      Set<String> mergedTags = new HashSet<>();
//      mergedTags.addAll(leftTags);
//      mergedTags.addAll(rightTags);
//      tags.addAll(mergedTags);
//      tags.add(attrName + NONE_UNKNOWN);
//    }
//    return tags;
//  }
//
//  private boolean isUnknown(List<String> leftTags) {
//    return leftTags.size() == 1 && leftTags.get(0).contains(UNKNOWN);
//  }
//
//  public static StringFrequencyAttributePairAnalyzer createFromFile(String location) {
//    return new StringFrequencyAttributePairAnalyzer(
//      StringFrequencyAnalyzer.createFromFile(location)
//    );
//  }
//}

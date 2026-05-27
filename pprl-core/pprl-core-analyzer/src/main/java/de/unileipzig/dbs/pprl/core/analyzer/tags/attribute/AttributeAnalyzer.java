package de.unileipzig.dbs.pprl.core.analyzer.tags.attribute;

import de.unileipzig.dbs.pprl.core.common.monitoring.Tag;

import java.util.List;

public interface AttributeAnalyzer<T> {

  List<Tag> getTags(String attrName, T attrValue);

}

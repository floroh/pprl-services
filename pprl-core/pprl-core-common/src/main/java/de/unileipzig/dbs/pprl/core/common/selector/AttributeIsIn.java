package de.unileipzig.dbs.pprl.core.common.selector;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class AttributeIsIn implements Selector<Record> {

  private String attributeName;
  private Set<String> lookupCollection;

  private boolean useUpperCase = true;

  @JsonIgnore
  private Set<String> lookupCollectionCased;

  public AttributeIsIn(String attributeName, Collection<String> lookupCollection) {
    this.attributeName = attributeName;
    this.lookupCollection = new LinkedHashSet<>(lookupCollection);
    if (useUpperCase) {
      lookupCollectionCased = lookupCollection.stream().map(String::toUpperCase).collect(Collectors.toSet());
    }
  }

  private AttributeIsIn() {
  }

  @Override
  public boolean test(Record record) {
    Optional<Attribute> optionalAttribute = record.getAttribute(attributeName);
    if (optionalAttribute.isEmpty()) {
      return false;
    }
    String asString = optionalAttribute.get().getAsString();
    if (useUpperCase) {
      asString = asString.toUpperCase();
    }
    return lookupCollectionCased.contains(asString);
  }

  public String getAttributeName() {
    return attributeName;
  }

  public Collection<String> getLookupCollection() {
    return lookupCollection;
  }

  public boolean isUseUpperCase() {
    return useUpperCase;
  }

  public void setUseUpperCase(boolean useUpperCase) {
    this.useUpperCase = useUpperCase;
    if (useUpperCase) {
      lookupCollectionCased = lookupCollection.stream().map(String::toUpperCase).collect(Collectors.toSet());
    }
  }
}

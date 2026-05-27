package de.unileipzig.dbs.pprl.service.dataowner.modifier.record;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.unileipzig.dbs.pprl.core.common.factories.AttributeFactory;
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public class AttributeCopyReplace implements RecordModifier {
  public static final String TAG_POSTFIX = "_COPY_REPLACE";

  private String originalAttributeName;

  private String additionalAttributeName;

  private List<String> values;

  private boolean replaceExisting = false;

  private long seed;

  @JsonIgnore
  private Random r = null;

  public AttributeCopyReplace(String originalAttributeName, String additionalAttributeName,
    List<String> values) {
    this(originalAttributeName, additionalAttributeName, values, 123);
  }

  public AttributeCopyReplace(String originalAttributeName, String additionalAttributeName,
    List<String> values, long seed) {
    this.originalAttributeName = originalAttributeName;
    this.additionalAttributeName = additionalAttributeName;
    this.values = values;
    this.seed = seed;
  }

  private AttributeCopyReplace() {
  }

  @Override
  public Record modify(Record in) {
    if (r == null) r = new Random(seed);

    Attribute originalAttribute = in.getAttribute(originalAttributeName).orElseThrow(() -> new RuntimeException("Missing " +
      "attribute: " + originalAttributeName));
    Record out = in.duplicate();

    String newValue = getValue(originalAttribute.getAsString());
    Attribute newAttribute = AttributeFactory.getAttribute(newValue);

    if (isEmpty(out.getAttribute(additionalAttributeName)) || replaceExisting) {
      out.setAttribute(additionalAttributeName, originalAttribute.duplicate());
    }
    out.setAttribute(originalAttributeName, newAttribute);

    return out;
  }

  @Override
  public List<String> getTags() {
    return List.of(originalAttributeName + "_" + additionalAttributeName + TAG_POSTFIX);
  }

  private String getValue(String old) {
    String r = getRandomValue();
    while (r.equals(old)) {
      r = getRandomValue();
    }
    return r;
  }

  private boolean isEmpty(Optional<Attribute> optionalAttribute) {
    return optionalAttribute.isEmpty() || optionalAttribute.get().getAsString().isEmpty();
  }

  private String getRandomValue() {
    return values.get(r.nextInt(values.size()));
  }

  public String getOriginalAttributeName() {
    return originalAttributeName;
  }

  public void setOriginalAttributeName(String originalAttributeName) {
    this.originalAttributeName = originalAttributeName;
  }

  public String getAdditionalAttributeName() {
    return additionalAttributeName;
  }

  public void setAdditionalAttributeName(String additionalAttributeName) {
    this.additionalAttributeName = additionalAttributeName;
  }

  public List<String> getValues() {
    return values;
  }

  public void setValues(List<String> values) {
    this.values = values;
  }

  public boolean isReplaceExisting() {
    return replaceExisting;
  }

  public void setReplaceExisting(boolean replaceExisting) {
    this.replaceExisting = replaceExisting;
  }

  public long getSeed() {
    return seed;
  }

  public void setSeed(long seed) {
    this.seed = seed;
  }
}

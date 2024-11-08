package de.unileipzig.dbs.pprl.core.common.frequencies;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static de.unileipzig.dbs.pprl.core.common.frequencies.AttributeFrequencyLookup.calculateTotal;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class AttributesFrequencyLookupFilter {

  private long absoluteTopLimit = -1;

  private double relativeTopLimit = -1.0;

  private long absoluteBottomLimit = -1;

  private double relativeBottomLimit = -1.0;

  private long minimumCount = 0;
  private static final Logger logger = LogManager.getLogger(AttributesFrequencyLookupFilter.class);

  public AttributeFrequencyLookup filter(AttributeFrequencyLookup afl) {
    return getFilteredAttributeFrequencyLookup(afl.getFrequencies());
  }

  public AttributeFrequencyLookup getFilteredAttributeFrequencyLookup(Map<String, Long> curFrequencies) {
    final long preFilteredUnique = curFrequencies.size();
    long preFilteredTotal = calculateTotal(curFrequencies);
    logger.debug("Pre Filtering: unique=" + preFilteredUnique + ", total=" + preFilteredTotal);

    Map<String, Long> minFilteredFrequencies = filterByMinCount(curFrequencies);
    final long minFilteredUnique = minFilteredFrequencies.size();
    long minFilteredTotal = calculateTotal(minFilteredFrequencies);
    logger.debug("After min filtering:: unique=" + minFilteredUnique + ", total=" + minFilteredTotal);

    final LinkedHashMap<String, Long> filteredFrequencies = filterFrequencies(minFilteredFrequencies);

    long totalValues = calculateTotal(filteredFrequencies);
    logger.debug("Post Filtering: unique=" + filteredFrequencies.size() + ", total=" + totalValues);
    logger.info("Removed " + (preFilteredUnique - filteredFrequencies.size()) + "/" + preFilteredUnique
      + " entries from the frequency lookup table");

    return new AttributeFrequencyLookup(filteredFrequencies, minFilteredTotal, minFilteredUnique);
  }

  private Map<String, Long> filterByMinCount(Map<String, Long> curFrequencies) {
    return curFrequencies.entrySet().stream()
      .filter(e -> e.getValue() >= minimumCount)
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private LinkedHashMap<String, Long> filterFrequencies(Map<String, Long> input) {
    LinkedHashMap<String, Long> filteredFrequencies = new LinkedHashMap<>();

    LinkedHashMap<String, Long> sortedFrequencies = AttributeFrequencyLookup.reverseSortByValue(input);
    long uniqueCount = sortedFrequencies.size();
    int absPos = 0;
    for (Map.Entry<String, Long> curFrequency : sortedFrequencies.entrySet()) {
      double relPos = (double) absPos / uniqueCount;
      boolean isInTop = isInTop(absPos, relPos);
      boolean isInBottom = isInBottom(absPos, relPos, uniqueCount);
      boolean checkTop = checkTop();
      boolean checkBottom = checkBottom();
      if (checkTop && checkBottom) {
        if (isInTop || isInBottom) {
          filteredFrequencies.put(curFrequency.getKey(), curFrequency.getValue());
        }
      } else {
        if ((checkTop ? isInTop : true) && (checkBottom ? isInBottom : true)) {
          filteredFrequencies.put(curFrequency.getKey(), curFrequency.getValue());
        }
      }
      absPos++;
    }
    return filteredFrequencies;
  }

  private boolean checkTop() {
    return isActive(absoluteTopLimit) || isActive(relativeTopLimit);
  }

  private boolean checkBottom() {
    return isActive(absoluteBottomLimit) || isActive(relativeBottomLimit);
  }

  private boolean isActive(double limit) {
    return limit >= 0;
  }

  private boolean isInTop(int absPos, double relPos) {
    boolean isInAbsolute = isActive(absoluteTopLimit) ?
      absPos < absoluteTopLimit
      : true;
    boolean isInRelative = isActive(relativeTopLimit) ?
      relPos < relativeTopLimit
      : true;
    return isInAbsolute && isInRelative;
  }

  private boolean isInBottom(int absPos, double relPos, long uniqueCount) {
    boolean isInAbsolute = isActive(absoluteBottomLimit) ?
      absPos >= uniqueCount - absoluteBottomLimit
      : true;
    boolean isInRelative = isActive(relativeBottomLimit) ?
      relPos >= 1 - relativeBottomLimit
      : true;
    return isInAbsolute && isInRelative;
  }

  public long getAbsoluteTopLimit() {
    return absoluteTopLimit;
  }

  public void setAbsoluteTopLimit(long absoluteTopLimit) {
    this.absoluteTopLimit = absoluteTopLimit;
  }

  public double getRelativeTopLimit() {
    return relativeTopLimit;
  }

  public void setRelativeTopLimit(double relativeTopLimit) {
    this.relativeTopLimit = relativeTopLimit;
  }

  public long getAbsoluteBottomLimit() {
    return absoluteBottomLimit;
  }

  public void setAbsoluteBottomLimit(long absoluteBottomLimit) {
    this.absoluteBottomLimit = absoluteBottomLimit;
  }

  public double getRelativeBottomLimit() {
    return relativeBottomLimit;
  }

  public void setRelativeBottomLimit(double relativeBottomLimit) {
    this.relativeBottomLimit = relativeBottomLimit;
  }

  public long getMinimumCount() {
    return minimumCount;
  }

  public void setMinimumCount(long minimumCount) {
    this.minimumCount = minimumCount;
  }

  @Override
  public String toString() {
    return "AttributesFrequencyLookupFilter{" +
            "absoluteTopLimit=" + absoluteTopLimit +
            ", relativeTopLimit=" + relativeTopLimit +
            ", absoluteBottomLimit=" + absoluteBottomLimit +
            ", relativeBottomLimit=" + relativeBottomLimit +
            ", minimumCount=" + minimumCount +
            '}';
  }
}

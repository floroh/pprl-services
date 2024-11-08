package de.unileipzig.dbs.pprl.core.common.frequencies;

import java.util.*;
import java.util.stream.Collectors;

public class AttributeFrequencyLookup {

  /**
   * Store frequencies of attributes
   * attribute value -> attribute frequency
   * The map is reverse sorted by the value.
   */
  private final LinkedHashMap<String, Long> frequencies;

  private long totalCount;

  private long uniqueCount;

  public AttributeFrequencyLookup(Map<String, Long> frequencies) {
    this(
      frequencies,
      calculateTotal(frequencies),
      frequencies.size()
    );
  }

  public AttributeFrequencyLookup(Map<String, Long> frequencies, long totalCount, long uniqueCount) {
    this.frequencies = reverseSortByValue(frequencies);
    this.totalCount = totalCount;
    this.uniqueCount = uniqueCount;
  }

  public Optional<Long> getFrequency(String attributeValue) {
    return Optional.ofNullable(frequencies.get(attributeValue));
  }

  public Optional<Double> getRelativeFrequency(String attributeValue) {
    Optional<Long> frequency = getFrequency(attributeValue);
    return frequency.map(f -> (double) f / getTotalCount());
  }

  public Optional<Double> getRelativeRank(String attributeValue) {
    Optional<Long> absoluteRank = getAbsoluteRank(attributeValue);
    return absoluteRank.map(r -> (double) r / getUniqueCount());
  }

  public Optional<Long> getAbsoluteRank(String attributeValue) {
    if (!frequencies.containsKey(attributeValue)) {
      return Optional.empty();
    }
    long rank = 1;
    for (Map.Entry<String, Long> entry : frequencies.entrySet()) {
      if (entry.getKey().equals(attributeValue)) {
        return Optional.of(rank);
      }
      rank++;
    }
    throw new RuntimeException("Should not be reached");
  }

  public long getHighestFrequency() {
    if (frequencies.isEmpty()) {
      return 0;
    }
    return frequencies.entrySet().iterator().next().getValue();
  }

  public long getLowestFrequency() {
    Iterator<Map.Entry<String, Long>> iterator = frequencies.entrySet().iterator();
    long lastValue = 1;
    while (iterator.hasNext()) {
      lastValue = iterator.next().getValue();
    }
    return lastValue;
  }

  public List<String> getAttributesReverseSortedByFrequency() {
    final List<String> values = new ArrayList<>();
    frequencies.keySet().iterator()
      .forEachRemaining(values::add);
    return values;
  }

  public Optional<String> getAbsoluteTopValue(int n) {
    Iterator<Map.Entry<String, Long>> iterator = frequencies.entrySet().iterator();
    int i = 1;
    while (iterator.hasNext()) {
      String value = iterator.next().getKey();
      if (i == n) {
        return Optional.of(value);
      }
      i++;
    }
    return Optional.empty();
  }

  public Optional<String> getMedianValue() {
    List<String> list = frequencies.entrySet().stream()
      .flatMap(v -> Collections.nCopies(v.getValue().intValue(), v.getKey()).stream())
      .collect(Collectors.toList());
    if (list.isEmpty()) {
      return Optional.empty();
    }
    int pos = list.size() % 2 == 0 ?
      list.size() / 2 - 1
      : list.size() / 2;
    String value = list.get(pos);
    return Optional.of(value);
  }

  public long getTotalCount() {
    return totalCount;
  }

  public void setTotalCount(long totalCount) {
    this.totalCount = totalCount;
  }

  public long getUniqueCount() {
    return uniqueCount;
  }

  public void setUniqueCount(long uniqueCount) {
    this.uniqueCount = uniqueCount;
  }

  public LinkedHashMap<String, Long> getFrequencies() {
    return frequencies;
  }

  public static long calculateTotal(Map<String, Long> frequencies) {
    return frequencies.values().stream().mapToLong(l -> l).sum();
  }

  public static <K, V extends Comparable<? super V>> LinkedHashMap<K, V> reverseSortByValue(Map<K, V> map) {
    List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
    list.sort(Map.Entry.<K, V>comparingByValue().reversed());

    LinkedHashMap<K, V> result = new LinkedHashMap<>();
    for (Map.Entry<K, V> entry : list) {
      result.put(entry.getKey(), entry.getValue());
    }
    return result;
  }

  @Override
  public String toString() {
    return "AttributeFrequencyLookup{" +
      "frequencies=" + frequencies +
      ", totalCount=" + totalCount +
      '}';
  }
}

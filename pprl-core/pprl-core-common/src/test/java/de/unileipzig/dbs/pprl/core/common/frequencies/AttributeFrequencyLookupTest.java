package de.unileipzig.dbs.pprl.core.common.frequencies;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AttributeFrequencyLookupTest {

  private static Map<String, Long> frequencies;

  @BeforeAll
  static void beforeAll() {
    frequencies = Map.ofEntries(
      Map.entry("B", 9L),
      Map.entry("A", 10L),
      Map.entry("C", 8L),
      Map.entry("D", 6L),
      Map.entry("E", 5L),
      Map.entry("F", 5L),
      Map.entry("G", 4L),
      Map.entry("H", 3L),
      Map.entry("J", 1L),
      Map.entry("I", 2L)
    );
  }

  @Test
  void getMedianValue() {
    AttributeFrequencyLookup filteredAFL = new AttributeFrequencyLookup(frequencies);
    Optional<String> medianValue = filteredAFL.getMedianValue();
    assertTrue(medianValue.isPresent());
    assertEquals("C", medianValue.get());
  }

  @Test
  void getAbsoluteTop() {
    AttributeFrequencyLookup filteredAFL = new AttributeFrequencyLookup(frequencies);
    Optional<String> top1 = filteredAFL.getAbsoluteTopValue(1);
    assertTrue(top1.isPresent());
    assertEquals("A", top1.get());
    Optional<String> top7 = filteredAFL.getAbsoluteTopValue(7);
    assertTrue(top7.isPresent());
    assertEquals("G", top7.get());
  }

  @Test
  void getRelativeFrequency() {
    AttributeFrequencyLookup filteredAFL = new AttributeFrequencyLookup(frequencies);
    Optional<Double> relFreq = filteredAFL.getRelativeFrequency("C");
    assertTrue(relFreq.isPresent());
    double expectedValue = frequencies.get("C") / (double) frequencies.values().stream().reduce(0L,
      Long::sum);
    assertEquals(expectedValue, relFreq.get());
    relFreq = filteredAFL.getRelativeFrequency("UNKNOWN");
    assertFalse(relFreq.isPresent());
  }
  @Test
  void getAbsoluteRank() {
    AttributeFrequencyLookup filteredAFL = new AttributeFrequencyLookup(frequencies);
    Optional<Long> absoluteRank = filteredAFL.getAbsoluteRank("C");
    assertTrue(absoluteRank.isPresent());
    assertEquals(3, absoluteRank.get());
  }
  @Test
  void getRelativeRank() {
    AttributeFrequencyLookup filteredAFL = new AttributeFrequencyLookup(frequencies);
    Optional<Double> relativeRank = filteredAFL.getRelativeRank("C");
    assertTrue(relativeRank.isPresent());
    assertEquals(3.0/frequencies.size(), relativeRank.get());
  }
}
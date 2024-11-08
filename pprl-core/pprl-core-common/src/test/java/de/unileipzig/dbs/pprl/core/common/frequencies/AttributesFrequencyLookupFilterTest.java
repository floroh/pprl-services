package de.unileipzig.dbs.pprl.core.common.frequencies;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static de.unileipzig.dbs.pprl.core.common.frequencies.AttributeFrequencyLookup.calculateTotal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttributesFrequencyLookupFilterTest {

  private static Map<String, Long> frequencies;

  @BeforeAll
  static void beforeAll() {
    frequencies = Map.ofEntries(
      Map.entry("B", 90L),
      Map.entry("A", 100L),
      Map.entry("C", 80L),
      Map.entry("D", 70L),
      Map.entry("E", 60L),
      Map.entry("F", 50L),
      Map.entry("G", 40L),
      Map.entry("H", 30L),
      Map.entry("J", 10L),
      Map.entry("I", 20L)
    );
  }

  @Test
  void filterByMinimumCount() {
    AttributesFrequencyLookupFilter filter = new AttributesFrequencyLookupFilter();
    filter.setMinimumCount(60);
    AttributeFrequencyLookup filteredAFL =
      filter.getFilteredAttributeFrequencyLookup(frequencies);
    assertEquals(5, filteredAFL.getFrequencies().size());
    assertEquals(5, filteredAFL.getUniqueCount());
    assertEquals(400, filteredAFL.getTotalCount());
  }

  @Test
  void filterAbsoluteTop() {
    AttributesFrequencyLookupFilter filter = new AttributesFrequencyLookupFilter();
    filter.setAbsoluteTopLimit(4);
    AttributeFrequencyLookup filteredAFL =
      filter.getFilteredAttributeFrequencyLookup(frequencies);
    assertEquals(4, filteredAFL.getFrequencies().size());
    assertEquals(frequencies.size(), filteredAFL.getUniqueCount());
    assertEquals(calculateTotal(frequencies), filteredAFL.getTotalCount());
  }

  @Test
  void filterAbsoluteBottom() {
    AttributesFrequencyLookupFilter filter = new AttributesFrequencyLookupFilter();
    filter.setAbsoluteBottomLimit(3);
    AttributeFrequencyLookup filteredAFL =
      filter.getFilteredAttributeFrequencyLookup(frequencies);
    assertEquals(3, filteredAFL.getFrequencies().size());
    assertEquals(frequencies.size(), filteredAFL.getUniqueCount());
    assertEquals(calculateTotal(frequencies), filteredAFL.getTotalCount());
  }

  @Test
  void filterAbsoluteTopAndBottom() {
    AttributesFrequencyLookupFilter filter = new AttributesFrequencyLookupFilter();
    filter.setAbsoluteTopLimit(4);
    filter.setAbsoluteBottomLimit(3);
    AttributeFrequencyLookup filteredAFL =
      filter.getFilteredAttributeFrequencyLookup(frequencies);
    assertEquals(7, filteredAFL.getFrequencies().size());
    assertEquals(frequencies.size(), filteredAFL.getUniqueCount());
    assertEquals(calculateTotal(frequencies), filteredAFL.getTotalCount());
  }

  @Test
  void filterRelativeBottom() {
    AttributesFrequencyLookupFilter filter = new AttributesFrequencyLookupFilter();
    filter.setRelativeBottomLimit(0.5);
    AttributeFrequencyLookup filteredAFL =
      filter.getFilteredAttributeFrequencyLookup(frequencies);
    assertTrue(filteredAFL.getFrequencies().size() < frequencies.size());
    assertEquals(frequencies.size(), filteredAFL.getUniqueCount());
    assertEquals(calculateTotal(frequencies), filteredAFL.getTotalCount());
    assertEquals(frequencies.size() * 0.5, filteredAFL.getFrequencies().size());
  }

  @Test
  void filterRelativeTopAndBottom() {
    AttributesFrequencyLookupFilter filter = new AttributesFrequencyLookupFilter();
    filter.setRelativeTopLimit(0.1);
    filter.setRelativeBottomLimit(0.5);
    AttributeFrequencyLookup filteredAFL =
      filter.getFilteredAttributeFrequencyLookup(frequencies);
    assertEquals(6, filteredAFL.getFrequencies().size());
    assertEquals(frequencies.size(), filteredAFL.getUniqueCount());
    assertEquals(calculateTotal(frequencies), filteredAFL.getTotalCount());
  }

  @Test
  void filterAllRelativeTopAndBottom() {
    AttributesFrequencyLookupFilter filter = new AttributesFrequencyLookupFilter();
    filter.setRelativeTopLimit(0.0);
    filter.setRelativeBottomLimit(0.0);
    AttributeFrequencyLookup filteredAFL =
      filter.getFilteredAttributeFrequencyLookup(frequencies);
    assertEquals(0, filteredAFL.getFrequencies().size());
    assertEquals(frequencies.size(), filteredAFL.getUniqueCount());
    assertEquals(calculateTotal(frequencies), filteredAFL.getTotalCount());
  }

  @Test
  void filterRelativeTopAndBottomOneInactive() {
    AttributesFrequencyLookupFilter filter = new AttributesFrequencyLookupFilter();
    filter.setRelativeTopLimit(0.3);
    filter.setRelativeBottomLimit(-1.0);
    AttributeFrequencyLookup filteredAFL =
      filter.getFilteredAttributeFrequencyLookup(frequencies);
    assertEquals(3, filteredAFL.getFrequencies().size());

    AttributesFrequencyLookupFilter filter2 = new AttributesFrequencyLookupFilter();
    filter2.setRelativeTopLimit(0.4);
    filter2.setRelativeBottomLimit(0.0);
    AttributeFrequencyLookup filteredAFL2 =
      filter2.getFilteredAttributeFrequencyLookup(frequencies);
    assertEquals(4, filteredAFL2.getFrequencies().size());
    assertEquals(frequencies.size(), filteredAFL.getUniqueCount());
    assertEquals(calculateTotal(frequencies), filteredAFL.getTotalCount());
  }
}
package de.unileipzig.dbs.pprl.service.generator.selection;

import de.unileipzig.dbs.pprl.core.common.model.impl.PersonalAttributeType;
import de.unileipzig.dbs.pprl.core.common.selector.AttributeIsIn;
import de.unileipzig.dbs.pprl.core.common.selector.SelectorCombination;
import de.unileipzig.dbs.pprl.service.generator.data.dto.UsvrSelectionConfig;
import de.unileipzig.dbs.pprl.service.generator.selection.model.common.ClusterPairCandidate;
import de.unileipzig.dbs.pprl.service.generator.selection.model.common.GenericRawRecord;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class RecordClusterSelectorTest {

  public static final ClusterPairCandidate CANDIDATE = ClusterPairCandidate.builder()
          .left(GenericRawRecord.builder().attribute("FN", "Peter").build())
          .right(GenericRawRecord.builder().attribute("FN", "Petra").build())
          .timespanInDays(Set.of(365, 400, 10))
          .changes(Map.of("FN", false, "LN", true, "DOB", false, "ADDR", true))
          .build();

  @Test
  void passesContentFilter() {
    UsvrSelectionConfig.ContentFilter contentFilter = new UsvrSelectionConfig.ContentFilter();
    contentFilter.setRecordSelector(new SelectorCombination<>(
            SelectorCombination.Operation.AND,
            new AttributeIsIn("FN", List.of("Peter"))
    ));
    UsvrSelectionConfig config = UsvrSelectionConfig.builder()
            .contentFilter(contentFilter)
            .build();
    assertTrue(RecordClusterSelector.passesContentFilter(CANDIDATE, config));
  }

  @Test
  void failsContentFilterBoth() {
    UsvrSelectionConfig.ContentFilter contentFilter = new UsvrSelectionConfig.ContentFilter();
    contentFilter.setBothRecordMatch(true);
    contentFilter.setRecordSelector(new SelectorCombination<>(
            SelectorCombination.Operation.AND,
            new AttributeIsIn("FN", List.of("Peter"))
    ));
    UsvrSelectionConfig config = UsvrSelectionConfig.builder()
            .contentFilter(contentFilter)
            .build();
    assertFalse(RecordClusterSelector.passesContentFilter(CANDIDATE, config));
  }

  @Test
  void failsContentFilter() {
    UsvrSelectionConfig.ContentFilter contentFilter = new UsvrSelectionConfig.ContentFilter();
    contentFilter.setRecordSelector(new SelectorCombination<>(
            SelectorCombination.Operation.AND,
            new AttributeIsIn("FN", List.of("def"))
    ));
    UsvrSelectionConfig config = UsvrSelectionConfig.builder()
            .contentFilter(contentFilter)
            .build();
    assertFalse(RecordClusterSelector.passesContentFilter(CANDIDATE, config));
  }


  @Test
  void contentFilterSerialization() {
    UsvrSelectionConfig.ContentFilter contentFilter = new UsvrSelectionConfig.ContentFilter();
//    List<String> lookupCollection = IntStream.rangeClosed(1900, 1949)
//    List<String> lookupCollection = IntStream.rangeClosed(1950, 1979)
    List<String> lookupCollection = IntStream.rangeClosed(1980, 2009)
            .mapToObj(Integer::toString)
            .toList();
    contentFilter.setRecordSelector(new SelectorCombination<>(
            SelectorCombination.Operation.AND,
            new AttributeIsIn(PersonalAttributeType.YEAROFBIRTH.name(), lookupCollection)
    ));
    String recordSelectorString = contentFilter.getRecordSelectorString();
    System.out.println(recordSelectorString);
  }


  @Test
  void passesTimeFilter() {
    UsvrSelectionConfig config = UsvrSelectionConfig.builder()
            .timeFilter(UsvrSelectionConfig.TimeFilter.builder().minDays(200).build())
            .build();
    assertTrue(RecordClusterSelector.passesTimeFilter(CANDIDATE, config));
  }

  @Test
  void passesTimeFilter2() {
    UsvrSelectionConfig config = UsvrSelectionConfig.builder()
            .timeFilter(UsvrSelectionConfig.TimeFilter.builder().minDays(100).maxDays(370).build())
            .build();
    assertTrue(RecordClusterSelector.passesTimeFilter(CANDIDATE, config));
  }

  @Test
  void passesTimeFilter3() {
    UsvrSelectionConfig config = UsvrSelectionConfig.builder()
            .timeFilter(UsvrSelectionConfig.TimeFilter.builder().minDays(2 * 365).build())
            .build();
    assertFalse(RecordClusterSelector.passesTimeFilter(CANDIDATE, config));
  }

  @Test
  void passesChangeFilter() {
    UsvrSelectionConfig config = UsvrSelectionConfig.builder()
            .changeFilter(UsvrSelectionConfig.ChangeFilter.builder().minChanges(3).build())
            .build();
    assertFalse(RecordClusterSelector.passesChangeFilter(CANDIDATE, config));
  }

  @Test
  void passesChangeFilter2() {
    UsvrSelectionConfig config = UsvrSelectionConfig.builder()
            .changeFilter(UsvrSelectionConfig.ChangeFilter.builder().minChanges(1).build())
            .build();
    assertTrue(RecordClusterSelector.passesChangeFilter(CANDIDATE, config));
  }

  @Test
  void passesChangeFilter3() {
    UsvrSelectionConfig config = UsvrSelectionConfig.builder()
            .changeFilter(UsvrSelectionConfig.ChangeFilter.builder()
                    .minChanges(2)
                    .changedAttributes(List.of("LN"))
                    .build())
            .build();
    assertTrue(RecordClusterSelector.passesChangeFilter(CANDIDATE, config));
  }

  @Test
  void passesChangeFilter4() {
    UsvrSelectionConfig config = UsvrSelectionConfig.builder()
            .changeFilter(UsvrSelectionConfig.ChangeFilter.builder()
                    .minChanges(1)
                    .changedAttributes(List.of("FN"))
                    .build())
            .build();
    assertFalse(RecordClusterSelector.passesChangeFilter(CANDIDATE, config));
  }

  @Test
  void passesChangeFilter5() {
    UsvrSelectionConfig config = UsvrSelectionConfig.builder()
            .changeFilter(UsvrSelectionConfig.ChangeFilter.builder()
                    .minChanges(2)
                    .changedAttributes(List.of("FN", "LN"))
                    .build())
            .build();
    assertTrue(RecordClusterSelector.passesChangeFilter(CANDIDATE, config));
  }

  @Test
  void passesChangeFilter6() {
    UsvrSelectionConfig config = UsvrSelectionConfig.builder()
            .changeFilter(UsvrSelectionConfig.ChangeFilter.builder()
                    .minChanges(2)
                    .changedAttributes(List.of("FN", "LN"))
                    .requireAllListedAttributes(true)
                    .build())
            .build();
    assertFalse(RecordClusterSelector.passesChangeFilter(CANDIDATE, config));
  }
}
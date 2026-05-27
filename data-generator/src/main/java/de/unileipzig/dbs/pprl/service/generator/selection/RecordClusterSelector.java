package de.unileipzig.dbs.pprl.service.generator.selection;

import de.unileipzig.dbs.pprl.core.common.factories.AttributeFactory;
import de.unileipzig.dbs.pprl.core.common.factories.RecordFactory;
import de.unileipzig.dbs.pprl.core.common.model.impl.PersonalAttributeType;
import de.unileipzig.dbs.pprl.core.common.selector.Selector;
import de.unileipzig.dbs.pprl.service.generator.data.dto.UsvrSelectionConfig;
import de.unileipzig.dbs.pprl.service.generator.selection.model.common.*;
import lombok.extern.slf4j.Slf4j;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Generates at most one ClusterPairContext per cluster using the selection config.
 */
@Slf4j
public class RecordClusterSelector {

  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

  public enum SIDE {
    A,
    B
  }

  private final UsvrSelectionConfig configuration;
  private final Set<String> fingerprints = new HashSet<>();

  public RecordClusterSelector(UsvrSelectionConfig configuration) {
    this.configuration = configuration;
  }

  public Optional<GenericRawRecord> selectRecordFromCluster(RecordCluster cluster, SIDE side) {
    List<GenericRawRecordWithDates> rawRecords = new ArrayList<>();
    rawRecords.add(cluster.getOrigin());
    rawRecords.addAll(cluster.getDuplicates());
    for (GenericRawRecordWithDates rawRecord : rawRecords) {
      if (side.equals(SIDE.A)) {
        if (configuration.getSnapshotDateA() != null) {
          if (rawRecord.getOccursIn().stream()
                  .noneMatch(d -> d.asString().equals(configuration.getSnapshotDateA()))
          ) continue;
        }
      }
      if (side.equals(SIDE.B)) {
        if (configuration.getSnapshotDateB() != null) {
          if (rawRecord.getOccursIn().stream()
                  .noneMatch(d -> d.asString().equals(configuration.getSnapshotDateB()))
          ) continue;
        }
      }
      String fingerPrint = RecordOutput.getCombinedPersonalAttributes(rawRecord.getRecord());
      if (fingerprints.contains(fingerPrint)) continue;

      if (!passesContentFilter(rawRecord.getRecord(), configuration)) {
        continue;
      }

      fingerprints.add(fingerPrint);
      return Optional.of(rawRecord.getRecord());
    }
    return Optional.empty();
  }

  /**
   * Generate a single pair for the given cluster according to config.
   * If no candidate satisfies the filters, returns Optional.empty().
   */
  public Optional<ClusterPairCandidate> generatePairFromCluster(RecordCluster cluster) {
//    log.debug("Checking cluster {} for pairs.", cluster.getId().toHexString());
    GenericRawRecordWithDates origin = cluster.getOrigin();
    if (configuration.getSnapshotDateA() != null) {
      if (origin.getOccursIn().stream()
              .noneMatch(d -> d.asString().equals(configuration.getSnapshotDateA()))
      ) return Optional.empty();
    }
    if (configuration.getSnapshotDateB() != null) {
      if (origin.getOccursIn().stream()
              .anyMatch(d -> d.asString().equals(configuration.getSnapshotDateB()))
      ) return Optional.empty();
    }
    final RecordOutput selectedOrigin =
            new RecordOutput("A", cluster.getDomainId(), cluster.getOrigin().getRecord());
    final String fingerprintOrigin = selectedOrigin.getCombinedPersonalAttributes();
    if (fingerprints.contains(fingerprintOrigin)) return Optional.empty();

    List<ClusterPairCandidate> candidates = new ArrayList<>();
    for (Duplicate duplicate : cluster.getDuplicates()) {
      if (configuration.getSnapshotDateB() != null) {
        if (duplicate.getOccursIn().stream()
                .noneMatch(d -> d.asString().equals(configuration.getSnapshotDateB()))
        ) continue;
      }
      Map<String, Boolean> changes = duplicate.getChanges();
      List<String> attributeColumns = configuration.getAttributeColumns();
      if (attributeColumns != null && !attributeColumns.isEmpty()) {
        changes = filterMapByKey(changes, attributeColumns);
      }

      ClusterPairCandidate candidate = ClusterPairCandidate.builder()
              .left(origin.getRecord())
              .right(duplicate.getRecord())
              .changes(changes)
              .timespanInDays(duplicate.getTimespanInDays())
              .build();
      if (configuration.isFixYobJitter()) {
        String yob = PersonalAttributeType.YEAROFBIRTH.name();
        candidate.getRight().getAttributes().put(yob,
                candidate.getLeft().getAttributes().get(yob));
        changes.put(yob, false);
      }
      fingerprints.add(RecordOutput.getCombinedPersonalAttributes(origin.getRecord()));
      fingerprints.add(RecordOutput.getCombinedPersonalAttributes(duplicate.getRecord()));
      candidates.add(candidate);
      break; // Currently break after first duplicate
    }

    List<ClusterPairCandidate> filtered = candidates.stream()
            .filter(c -> passesChangeFilter(c, configuration))
            .filter(c -> passesTimeFilter(c, configuration))
            .filter(c -> passesContentFilter(c, configuration))
            .toList();

    if (filtered.isEmpty()) return Optional.empty();

    // In future there may be multiple possible candidates and the winner must be determined deterministically
    ClusterPairCandidate winner = filtered.getFirst();
    return Optional.of(winner);
  }

  public static Map<String, Boolean> filterMapByKey(Map<String, Boolean> changes, Collection<String> keys) {
    return changes.entrySet().stream()
            .filter(entry -> keys.contains(entry.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  public static boolean passesTimeFilter(ClusterPairCandidate c, UsvrSelectionConfig cfg) {
    var tf = cfg.getTimeFilter();
    if (tf == null) return true;
    Predicate<Integer> timespanTest = d -> {
      if (tf.getIgnoreOrder()) {
        d = Math.abs(d);
      }
      boolean meetsMin = (tf.getMinDays() == null) || (d >= tf.getMinDays());
      boolean meetsMax = (tf.getMaxDays() == null) || (d <= tf.getMaxDays());
      if (tf.getRequireAllTimestampsPassing()) {
        return !(meetsMin && meetsMax);
      }
      return meetsMin && meetsMax;
    };

    if (tf.getRequireAllTimestampsPassing()) {
      return c.getTimespanInDays().stream()
              .filter(Objects::nonNull)
              .noneMatch(timespanTest);
    }

    return c.getTimespanInDays().stream()
            .filter(Objects::nonNull)
            .anyMatch(timespanTest);
  }

  public static boolean passesChangeFilter(ClusterPairCandidate c, UsvrSelectionConfig cfg) {
    var cf = cfg.getChangeFilter();
    if (cf == null) return true;
    int numChanges = c.getNumChanges();
    if (cf.getMinChanges() != null && numChanges < cf.getMinChanges()) return false;
    if (cf.getMaxChanges() != null && numChanges > cf.getMaxChanges()) return false;
    if (cf.getChangedAttributes() != null && !cf.getChangedAttributes().isEmpty()) {
      if (cf.isRequireAllListedAttributes()) {
        return c.getChangedAttributes().containsAll(cf.getChangedAttributes());
      } else {
        // require at least one of the listed attributes changed
        for (String k : cf.getChangedAttributes()) {
          if (c.getChangedAttributes().contains(k)) return true;
        }
        return false;
      }
    }
    return true;
  }

  public static boolean passesContentFilter(ClusterPairCandidate c, UsvrSelectionConfig cfg) {
    var cf = cfg.getContentFilter();
    if (cf == null) return true;
    long passes = Stream.of(c.getLeft(), c.getRight())
            .filter(gr -> RecordClusterSelector.passesContentFilter(gr, cfg))
            .count();
    if (cf.getBothRecordMatch() != null && cf.getBothRecordMatch()) return passes == 2;
    return passes > 0;
  }

  public static boolean passesContentFilter(GenericRawRecord gr, UsvrSelectionConfig cfg) {
    var cf = cfg.getContentFilter();
    if (cf == null) return true;
    Selector<de.unileipzig.dbs.pprl.core.common.model.api.Record> filter = cf.getRecordSelector();
    return Stream.of(gr)
            .map(grr -> {
              de.unileipzig.dbs.pprl.core.common.model.api.Record record = RecordFactory.getEmptyRecord();
              grr.getAttributes().forEach((k, v) -> record.setAttribute(k, AttributeFactory.getAttribute(v)));
              return record;
            })
            .anyMatch(filter);
  }
}
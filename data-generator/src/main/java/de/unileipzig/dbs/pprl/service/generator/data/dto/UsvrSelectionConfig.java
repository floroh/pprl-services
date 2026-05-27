package de.unileipzig.dbs.pprl.service.generator.data.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.unileipzig.dbs.pprl.core.common.exceptions.PprlException;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.selector.Selector;
import de.unileipzig.dbs.pprl.core.encoder.record.RecordEncoder;
import de.unileipzig.dbs.pprl.service.generator.selection.model.common.ClusterType;
import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UsvrSelectionConfig {

  /* Basic metadata */
  private String datasetVariantSuffix;

  @Builder.Default
  private ClusterType clusterType = ClusterType.NC;

  /* size */
  @Builder.Default
  private int numRecordsA = 10_000;
  @Builder.Default
  private int numRecordsB = 10_000;
  @Builder.Default
  private int numDuplicates = 2_000;

  /* cluster selection */
  @Builder.Default
  private int numClusters = 2_000;

  /* snapshot dates (ISO yyyy-MM-dd) */
  private String snapshotDateA;
  private String snapshotDateB;

  @Builder.Default
  private String sourceA = "A";
  @Builder.Default
  private String sourceB = "B";

  /* optional list of attributes to consider (null/empty = all attributes) */
  private List<String> attributeColumns;

  /* cluster ordering strategy */
  @Builder.Default
  private OrderingStrategy orderingStrategy = OrderingStrategy.SEEDED_SHUFFLE;
  @Builder.Default
  private String orderingSeed = null;

  /**
   * When the year of birth is computed based on the age and snapshot date, it may vary by +-1 year.
   * When setting this to true, the yob values of the cluster candidate pairs are adjusted to match
   */
  @Builder.Default
  private boolean fixYobJitter = true;

  /* filter DTOs */
  @Builder.Default
  private ChangeFilter changeFilter = null;
  @Builder.Default
  private TimeFilter timeFilter = null;
  @Builder.Default
  private ContentFilter contentFilter = null;

  @JsonIgnore
  public int getNumIndividualsA() {
    if (numRecordsA < numDuplicates) {
      throw new IllegalArgumentException("numRecordsA must be >= numDuplicates");
    }
    return numRecordsA - numDuplicates;
  }

  @JsonIgnore
  public int getNumIndividualsB() {
    if (numRecordsB < numDuplicates) {
      throw new IllegalArgumentException("numRecordsB must be >= numDuplicates");
    }
    return numRecordsB - numDuplicates;
  }

  /* ------- nested data-only DTOs ------- */

  public enum OrderingStrategy {
    SEEDED_SHUFFLE,
    DB_ORDER,
  }

  @Data
  @Builder
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class TimeFilter {
    /* Whether to use the absolute time distance instead of the potentially negative difference */
    @Builder.Default
    private Boolean ignoreOrder = Boolean.TRUE;

    @Builder.Default
    private Boolean requireAllTimestampsPassing = Boolean.FALSE;

    private Integer minDays;
    private Integer maxDays;

    public TimeFilter() {
      this.ignoreOrder = Boolean.TRUE;
      this.requireAllTimestampsPassing = Boolean.FALSE;
    }
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class ChangeFilter {
    private Integer minChanges;
    private Integer maxChanges;
    private List<String> changedAttributes; // attributes of interest; null/empty == all
    private List<String> unchangedAttributes; // attributes of interest; null/empty == all

    // if true, all listed attributes must have a change, otherwise only one
    @Builder.Default
    private boolean requireAllListedAttributes = false;
  }

  @Data
  @Builder
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class ContentFilter {
    private String recordSelectorString;

    @JsonIgnore
    private Selector<Record> recordSelector;

    private Boolean bothRecordMatch = Boolean.FALSE;

    public ContentFilter() {
      this.bothRecordMatch = Boolean.FALSE;
    }

    public ContentFilter(Selector<Record> recordSelector) {
      this();
      this.recordSelector = recordSelector;
    }

    public ContentFilter(Selector<Record> recordSelector, Boolean bothRecordMatch) {
      this.recordSelector = recordSelector;
      this.bothRecordMatch = bothRecordMatch;
    }

    public ContentFilter(String recordSelectorString) {
      this();
      this.recordSelectorString = recordSelectorString;
    }

    public ContentFilter(String recordSelectorString, Boolean bothRecordMatch) {
      this.recordSelectorString = recordSelectorString;
      this.bothRecordMatch = bothRecordMatch;
    }

    public Selector<Record> getRecordSelector() {
      if (recordSelector == null) {
        try {
          recordSelector = new ObjectMapper().readValue(recordSelectorString, Selector.class);
        } catch (JsonProcessingException e) {
          throw new PprlException("Failed to parse recordsSelector");
        }
      }
      return recordSelector;
    }

    public void setRecordSelector(Selector<Record> recordSelector) {
      this.recordSelector = recordSelector;
      try {
        this.recordSelectorString = new ObjectMapper().writeValueAsString(recordSelector);
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e.fillInStackTrace());
      }
    }

    @Override
    public String toString() {
      return "ContentFilter{" +
              "recordSelectorString='" + recordSelectorString + '\'' +
              ", bothRecordMatch=" + bothRecordMatch +
              '}';
    }
  }

  @JsonIgnore
  public String getName(boolean includeFilters, boolean includeSnapshots, boolean includeSeed) {
    StringBuilder sb = new StringBuilder();

    // Base structural description
    sb.append(clusterType != null ? clusterType.name() : "NC");
    sb.append("_A").append(numRecordsA);
    sb.append("_B").append(numRecordsB);
    sb.append("_dup").append(numDuplicates);

    // optional seed
    if (includeSeed && orderingSeed != null && !orderingSeed.isBlank()) {
      sb.append("_seed").append(orderingSeed);
    }

    // optional snapshot info
    if (includeSnapshots) {
      if (snapshotDateA != null) sb.append("_snapA").append(snapshotDateA);
      if (snapshotDateB != null) sb.append("_snapB").append(snapshotDateB);
    }

    // optional filters
    if (includeFilters) {
      if (changeFilter != null) {
        sb.append("_chg");
        if (changeFilter.getMinChanges() != null || changeFilter.getMaxChanges() != null) {
          sb.append("[");
          if (changeFilter.getMinChanges() != null) sb.append("min=").append(changeFilter.getMinChanges());
          if (changeFilter.getMaxChanges() != null) {
            if (changeFilter.getMinChanges() != null) sb.append(",");
            sb.append("max=").append(changeFilter.getMaxChanges());
          }
          if (changeFilter.isRequireAllListedAttributes()) sb.append(",all");
          sb.append("]");
        }
      }

      if (timeFilter != null) {
        sb.append("_time(");
        if (timeFilter.getMinDays() != null) sb.append("min=").append(timeFilter.getMinDays());
        if (timeFilter.getMaxDays() != null) sb.append(",max=").append(timeFilter.getMaxDays());
        if (timeFilter.getIgnoreOrder()) sb.append(",ign");
        if (timeFilter.getRequireAllTimestampsPassing()) sb.append(",all");
        sb.append(")");
      }

      if (contentFilter != null) {
        sb.append("_cont");
        if (Boolean.TRUE.equals(contentFilter.getBothRecordMatch())) {
          sb.append("(bothMatch)");
        }
      }
    }

    // optional variant suffix
    if (datasetVariantSuffix != null && !datasetVariantSuffix.isBlank()) {
      sb.append("_").append(datasetVariantSuffix);
    }

    return sb.toString();
  }
}

package de.unileipzig.dbs.pprl.service.generator.data.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GermanyGeneratorConfig {
  private String seed;
  private int numberOfRecords;
  private boolean includeHouseholdStructures;
  private boolean includeHeader;
  private String destinationFolder;
  private String fileName;
  private List<String> attributes;

  @Builder.Default
  private String sourceName = "A";

  @JsonIgnore
  public String getName() {
    return numberOfRecords + "records_" + (includeHouseholdStructures? "HH" : "noHH") + "_seed=" + seed;
  }
}

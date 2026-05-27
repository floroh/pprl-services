package de.unileipzig.dbs.pprl.service.dataowner.generator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.service.dataowner.modifier.DataSetModifierConfig;
import de.unileipzig.dbs.pprl.core.common.selector.Selector;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Data
@Builder
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property = "@class")
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Configuration for generating the corrupted dataset")
public class DataSetGeneratorConfig {

  @Schema(description = "Name of the configuration")
  private String name;

  @Schema(description = "Number of records from the original source")
  private Integer originalSize;

  @Schema(description = "Number of records from the modified source")
  private Integer modifiedSize;

  @Schema(description = "Share of records from original source that have a duplicate in modified source")
    private Double sourceOverlap;

  @Schema(description = "Filter the input records before using them in the generator")
  private Selector<Record> inputFilter;

  @Singular
  @Schema(description = "Modifiers to sequentially apply on the input records before creating the modified two-source dataset")
  private List<DataSetModifierConfig> sourceModifierConfigs;

  @Singular
  @Schema(description = "Modifiers to create true matches and true non-matches")
  private List<DataSetModifierConfig> duplicateModifierConfigs;

  @Schema(description = "Global seed for randomness, e.g., when shuffling the input records before corruption\n" +
          " Take care: Does not necessarily affect random selectors within modifier configs!")
  private Long seed;

  @Schema(description = "Setting on how to apply the duplicateModifierConfigs")
  @Builder.Default
  private ModifierDistributionStrategy modifierDistributionStrategy = ModifierDistributionStrategy.UNISIZE_RELAXED;

  public enum ModifierDistributionStrategy {
    UNISIZE,
    UNISIZE_RELAXED
  }

  @JsonIgnore
  public String getConfigName() {
    return name + (seed != null ? "_seed=" + seed: "");
  }
}

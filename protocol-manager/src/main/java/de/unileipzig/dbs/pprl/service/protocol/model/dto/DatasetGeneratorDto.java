package de.unileipzig.dbs.pprl.service.protocol.model.dto;

import de.unileipzig.dbs.pprl.service.generator.data.dto.GermanyGeneratorConfig;
import de.unileipzig.dbs.pprl.service.generator.data.dto.UsvrSelectionConfig;
import jakarta.validation.constraints.AssertTrue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatasetGeneratorDto {

  private Long datasetId;

  private String datasetName;

  private GermanyGeneratorConfig germanyGeneratorConfig;

  private UsvrSelectionConfig usvrSelectionConfig;

  @AssertTrue(message = "Exactly one of germanyGeneratorConfig or usvrSelectionConfig must be set")
  private boolean isConfigXorConfigCreator() {
    return (germanyGeneratorConfig == null) ^ (usvrSelectionConfig == null);
  }

}

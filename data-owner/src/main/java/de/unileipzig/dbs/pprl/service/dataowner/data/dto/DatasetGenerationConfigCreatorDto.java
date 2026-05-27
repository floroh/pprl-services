package de.unileipzig.dbs.pprl.service.dataowner.data.dto;

import de.unileipzig.dbs.pprl.service.dataowner.generator.DataSetGeneratorConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatasetGenerationConfigCreatorDto {

  @Schema(description = "ID of the dataset whose records and attribute value (frequencies) are used as the" +
          " source for generated records. If empty or 0, the input dataset will be used instead")
  private long referenceDatasetId;

  @NotEmpty
  @Schema(description = "Name of the dataset modification config")
  private String name;


  @Schema(description = "Configuration settings which override the generated config parameters")
  private DataSetGeneratorConfig override;

}

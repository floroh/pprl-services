package de.unileipzig.dbs.pprl.service.protocol.model.dto;

import de.unileipzig.dbs.pprl.service.common.data.dto.DatasetDto;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatasetCsvDto {

  @NotEmpty
  private String path;

  private DatasetDto datasetDto;

}

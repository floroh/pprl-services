package de.unileipzig.dbs.pprl.service.generator.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NcvrPanseImportRequest {

  @Builder.Default
  private Integer maxClusters = 0;

  @Builder.Default
  private boolean forceImportEvenWhenNotEmpty = false;

}

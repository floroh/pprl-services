package de.unileipzig.dbs.pprl.service.generator.data.dto;

import de.unileipzig.dbs.pprl.service.generator.selection.model.common.ClusterType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClusterOrderRequest {

  private ClusterType type;

  private String seed;
}

package de.unileipzig.dbs.pprl.service.linkageunit.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MatcherUpdateRequest {
  @NonNull
  private String projectId;

  private MatcherUpdateType type = MatcherUpdateType.NEW_IMPROVED;

}

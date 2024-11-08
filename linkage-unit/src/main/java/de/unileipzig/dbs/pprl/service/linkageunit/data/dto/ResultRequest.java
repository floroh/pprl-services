package de.unileipzig.dbs.pprl.service.linkageunit.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Builder
public class ResultRequest {
  @NonNull
  private String projectId;

  private Set<String> pairProperties;

}

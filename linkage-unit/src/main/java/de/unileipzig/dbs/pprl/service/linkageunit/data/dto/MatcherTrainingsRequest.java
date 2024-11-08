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
public class MatcherTrainingsRequest {
  @NonNull
  private MatcherIdDto matcherId;

  private int datasetId;

  private double minSimilarity;

  private double maxSimilarity;

}

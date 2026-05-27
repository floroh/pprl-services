package de.unileipzig.dbs.pprl.service.generator.selection.model.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClusterPairCandidate {

  private String clusterIdHex;    // cluster ObjectId as hex string (optional)
  private GenericRawRecord left;
  private GenericRawRecord right;

  private Map<String, Boolean> changes;
  private Set<Integer> timespanInDays;

  public int getNumChanges() {
    return changes.values().stream().mapToInt(aBoolean -> aBoolean ? 1 : 0).sum();
  }

  public Set<String> getChangedAttributes() {
    return changes.entrySet().stream()
            .filter(Map.Entry::getValue)
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
  }
}
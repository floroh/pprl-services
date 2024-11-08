package de.unileipzig.dbs.pprl.service.common.data.mongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordIdPair;
import de.unileipzig.dbs.pprl.core.matcher.classification.Classifier;
import de.unileipzig.dbs.pprl.core.matcher.evaluation.GroundTruth;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Document
@Data
@NoArgsConstructor
public class MongoGroundTruth {

  @Id
  private int datasetId;

  private Collection<MongoRecordIdPair> recordIdPairs;

  public MongoGroundTruth(int datasetId, Collection<MongoRecordIdPair> recordIdPairs) {
    this.datasetId = datasetId;
    this.recordIdPairs = recordIdPairs;
  }

  @JsonIgnore
  public GroundTruth getGroundTruth() {
    return GroundTruth.createFromLinks(
      recordIdPairs.stream()
        .filter(midp -> midp.getLabel().equals(Classifier.Label.TRUE_MATCH))
        .collect(Collectors.toList())
    );
  }

  public Set<String> getTrueMatchPairIds() {
    return recordIdPairs.stream()
      .filter(midp -> midp.getLabel().equals(Classifier.Label.TRUE_MATCH))
      .map(RecordIdPair::getPairId)
      .collect(Collectors.toSet());
  }
}

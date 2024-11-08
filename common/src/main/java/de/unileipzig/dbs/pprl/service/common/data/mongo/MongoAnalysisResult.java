package de.unileipzig.dbs.pprl.service.common.data.mongo;

import de.unileipzig.dbs.pprl.service.common.data.dto.analysis.AnalysisResultDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MongoAnalysisResult {

  private ObjectId id;

  private String source;

  private Type type;

  private int datasetId;

  private AnalysisResultDto result;

  public enum Type {
    DATASET_DESCRIPTION,
    VALIDATION
  }
}

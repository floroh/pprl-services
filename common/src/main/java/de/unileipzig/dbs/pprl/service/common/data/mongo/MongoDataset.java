package de.unileipzig.dbs.pprl.service.common.data.mongo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MongoDataset {

  @Id
  private int datasetId;

  private int plaintextDatasetId;

  private String datasetName;
}

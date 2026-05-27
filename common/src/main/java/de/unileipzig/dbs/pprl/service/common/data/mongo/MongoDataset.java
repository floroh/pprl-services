package de.unileipzig.dbs.pprl.service.common.data.mongo;

import de.unileipzig.dbs.pprl.service.common.data.dto.EncodingIdDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Document
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MongoDataset {

  @Id
  private Long datasetId;

  private String datasetName;

  private Long plaintextDatasetId;

  private EncodingIdDto encodingIdDto;

  @Singular
  private Map<String, String> properties;
}

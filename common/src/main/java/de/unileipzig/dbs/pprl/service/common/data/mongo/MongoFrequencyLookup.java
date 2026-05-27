package de.unileipzig.dbs.pprl.service.common.data.mongo;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Data
@Builder
@Document
public class MongoFrequencyLookup {

  @Id
  private ObjectId objectId;

  private long datasetId;

  private String datasetSource;

  private String config;

  @Singular
  private Map<String, Map<String, Long>> frequenciesByAttributes;
}

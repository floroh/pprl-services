package de.unileipzig.dbs.pprl.service.generator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "pprl.generator.mongo")
@Data
public class MongoConnectionConfig {
  public int importBatchSize = 1000;
  public int clusterOrderInsertBatchSize = 10000;
  public int clusterOrderRetrieveBatchSize = 1000;
}

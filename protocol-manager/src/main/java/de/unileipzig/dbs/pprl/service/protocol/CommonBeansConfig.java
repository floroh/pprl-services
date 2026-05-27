package de.unileipzig.dbs.pprl.service.protocol;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import de.unileipzig.dbs.pprl.service.common.services.DatasetIdService;

@Configuration
public class CommonBeansConfig {

  @Bean
  public DatasetIdService datasetIdService() {
    return new DatasetIdService();
  }
}
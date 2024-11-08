package de.unileipzig.dbs.pprl.service.protocol.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "pprl.services")
@Data
public class ServicesConfig {

  private String dataOwnerEndpoint;

  private String linkageUnitEndpoint;

}

package de.unileipzig.dbs.pprl.service.linkageunit.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "pprl.services.classifier")
@Data
public class ExternalClassifierConnectionConfig {

  private String endpoint = "http://localhost:8087";

  private String bearerToken = "secret";

}

package de.unileipzig.dbs.pprl.service.linkageunit.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "pprl.matcher")
@Data
public class MatcherConfig {

  private String incrementalName;

  private String batchName;

}

package de.unileipzig.dbs.pprl.service.linkageunit.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "pprl.lu.reporting")
@Data
public class LinkageUnitReportingConfig {

  private boolean skipBlockingReports = false;

}

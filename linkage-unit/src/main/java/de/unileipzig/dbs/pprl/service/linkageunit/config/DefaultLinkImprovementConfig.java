package de.unileipzig.dbs.pprl.service.linkageunit.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "pprl.network")
public class DefaultLinkImprovementConfig extends LinkImprovementConfig {

}

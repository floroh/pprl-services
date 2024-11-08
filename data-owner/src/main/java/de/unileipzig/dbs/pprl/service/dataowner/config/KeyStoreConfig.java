package de.unileipzig.dbs.pprl.service.dataowner.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "pprl.ks")
@Data
public class KeyStoreConfig {

  private String location;

  private String password;

}

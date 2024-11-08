package de.unileipzig.dbs.pprl.service.dataowner.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "pprl.encodings")
@Data
public class LocalEncodingsConfig {

  private List<String> paths;

}

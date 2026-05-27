package de.unileipzig.dbs.pprl.service.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "pprl.preprocessing")
@Data
public class PreprocessingConfig {
  private boolean splitDateOfBirthAttributeIfPossible = true;
  private String datePattern;
}

package de.unileipzig.dbs.pprl.service.generator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "datasets.usvr")
@Data
public class UsvrDbConfig {
	public String connectionString = "mongodb://root:example@localhost:27018/?authSource=admin";
	public String database = "usvr";
  public String ncPanseCollection = "2-testdata";

}

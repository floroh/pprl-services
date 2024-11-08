package de.unileipzig.dbs.pprl.service.dataowner.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "dataset.csv")
@Data
public class CsvDatasetConfig {

  private boolean replaceExisting = false;

  private List<SingleCsvDatasetConfig> datasetConfigs;

  @Data
  public static class SingleCsvDatasetConfig {

    private String location;

    private int datasetId;

    private String source;
  }
}

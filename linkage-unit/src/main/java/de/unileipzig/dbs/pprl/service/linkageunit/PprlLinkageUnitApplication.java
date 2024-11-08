package de.unileipzig.dbs.pprl.service.linkageunit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EntityScan(
  {"de.unileipzig.dbs.pprl.service.common.data.jpa", "de.unileipzig.dbs.pprl.service.linkageunit"})
@ComponentScan("de.unileipzig.dbs.pprl.service")
@EnableMongoRepositories({
  "de.unileipzig.dbs.pprl.service.common.persistence.repositories", "de.unileipzig.dbs.pprl.service" +
  ".linkageunit.persistence.repositories"
})
public class PprlLinkageUnitApplication {

  public static void main(String[] args) {
    SpringApplication.run(PprlLinkageUnitApplication.class, args);
  }

//  @Bean
//  MeterRegistryCustomizer<MeterRegistry> metricsLuCommonTags() {
//    return registry -> registry.config().commonTags("project", "pprl-lu-test");
//  }

}

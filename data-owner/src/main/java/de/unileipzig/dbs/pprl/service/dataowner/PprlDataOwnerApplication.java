package de.unileipzig.dbs.pprl.service.dataowner;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EntityScan("de.unileipzig.dbs.pprl.service.common.data.mongo")
@ComponentScan("de.unileipzig.dbs.pprl.service")
@EnableMongoRepositories(value = "de.unileipzig.dbs.pprl.service.common.persistence.repositories.mongo")
public class PprlDataOwnerApplication {

  public static void main(String[] args) {
    SpringApplication.run(PprlDataOwnerApplication.class, args);
  }

  @Bean
  public MappingMongoConverter mongoConverter(
    MongoDatabaseFactory mongoFactory, MongoMappingContext mongoMappingContext) {
    DbRefResolver dbRefResolver = new DefaultDbRefResolver(mongoFactory);
    MappingMongoConverter mongoConverter = new MappingMongoConverter(dbRefResolver, mongoMappingContext);
    mongoConverter.setMapKeyDotReplacement("-DOT");
    return mongoConverter;
  }

  @Bean
  MeterRegistryCustomizer<MeterRegistry> metricsDoCommonTags() {
    return registry -> registry.config().commonTags("project", "pprl-do-test");
  }

}

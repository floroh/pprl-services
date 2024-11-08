package de.unileipzig.dbs.pprl.service.protocol;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories({"de.unileipzig.dbs.pprl.service.protocol.persistence.repositories"})
public class PprlProtocolApplication {

  public static void main(String[] args) {
    SpringApplication.run(PprlProtocolApplication.class, args);
  }

  @Bean
  public MappingMongoConverter mongoConverter(
    MongoDatabaseFactory mongoFactory, MongoMappingContext mongoMappingContext) {
    DbRefResolver dbRefResolver = new DefaultDbRefResolver(mongoFactory);
    MappingMongoConverter mongoConverter = new MappingMongoConverter(dbRefResolver, mongoMappingContext);
    mongoConverter.setMapKeyDotReplacement("-DOT");
    return mongoConverter;
  }

}

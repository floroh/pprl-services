package de.unileipzig.dbs.pprl.service.generator.services;

import de.unileipzig.dbs.pprl.service.common.data.dto.RecordDto;
import de.unileipzig.dbs.pprl.service.generator.data.dto.GermanyGeneratorConfig;
import de.unileipzig.dbs.pprl.service.generator.data.dto.TaggedDatasetDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class GermanyGeneratorServiceTest {

  @Container
  @ServiceConnection
  static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:8.0")
          .waitingFor(Wait.forListeningPort())
          .withStartupTimeout(Duration.ofSeconds(60));


  @Autowired
  private GermanyGeneratorService germanyGeneratorService;

  @Test
  void generateNoHousehold() {
    GermanyGeneratorConfig config = GermanyGeneratorConfig.builder()
            .seed("abc42")
            .numberOfRecords(100)
            .includeHouseholdStructures(false)
            .build();
    TaggedDatasetDto datasetDto = germanyGeneratorService.generate(config);
    assertNotNull(datasetDto);
    List<RecordDto> records = datasetDto.getRecords();
    assertEquals(100, records.size());
  }
}
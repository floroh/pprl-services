package de.unileipzig.dbs.pprl.service.dataowner.persistence.repositories;

import de.unileipzig.dbs.pprl.core.common.model.impl.RecordIdComposed;
import de.unileipzig.dbs.pprl.core.common.model.impl.RecordIdMap;
import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoRecord;
import de.unileipzig.dbs.pprl.service.common.persistence.repositories.mongo.MongoRecordRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
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
@DataMongoTest
@ActiveProfiles("test")
public class RecordRepositoryTest {

  @Container
  @ServiceConnection
  static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:8.0")
          .waitingFor(Wait.forListeningPort())
          .withStartupTimeout(Duration.ofSeconds(60));

  @Autowired
  MongoRecordRepository mongoRecordRepository;


  @Test
  void connectionEstablished() {
    assertTrue(mongoDBContainer.isCreated());
    assertTrue(mongoDBContainer.isRunning());
  }

  @Test
  void addRecord() {
    MongoRecord record = new MongoRecord(23, new RecordIdMap("123", "A"));


    List<MongoRecord> storedRecords = mongoRecordRepository.findAll();
    assertEquals(0, storedRecords.size());

    mongoRecordRepository.save(record);
    storedRecords = mongoRecordRepository.findAll();
    assertEquals(1, storedRecords.size());
  }
}

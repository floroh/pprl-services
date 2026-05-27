package de.unileipzig.dbs.pprl.service.generator.services;

import de.unileipzig.dbs.pprl.core.common.selector.AttributeIsIn;
import de.unileipzig.dbs.pprl.core.common.selector.SelectorCombination;
import de.unileipzig.dbs.pprl.service.common.data.dto.AttributeDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordDto;
import de.unileipzig.dbs.pprl.service.generator.data.dto.TaggedDatasetDto;
import de.unileipzig.dbs.pprl.service.generator.data.dto.UsvrSelectionConfig;
import de.unileipzig.dbs.pprl.service.generator.selection.model.common.ClusterType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class UsSelectorServiceTest {

  @Container
  @ServiceConnection
  static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:8.0")
          .waitingFor(Wait.forListeningPort())
          .withStartupTimeout(Duration.ofSeconds(60));

  @Autowired
  private UsSelectorService usSelectorService;

  @BeforeAll
  static void beforeAll() throws IOException, InterruptedException {
    mongoDBContainer.copyFileToContainer(
            MountableFile.forClasspathResource("generator-us/ncvr_clusters_1000.json"),
            "/tmp/ncvr_clusters.json"
    );

    mongoDBContainer.execInContainer(
            "mongoimport",
            "--db=pprldg",
            "--collection=ncvr_clusters",
            "--file=/tmp/ncvr_clusters.json"
    );
  }

  @Test
  void connectionEstablished() {
    assertTrue(mongoDBContainer.isCreated());
    assertTrue(mongoDBContainer.isRunning());
  }

  @Test
  void getNcvrNoFilter() {
    UsvrSelectionConfig config = UsvrSelectionConfig.builder()
            .clusterType(ClusterType.NC)
            .numRecordsA(10)
            .numRecordsB(20)
            .numDuplicates(5)
            .orderingSeed("abcd")
            .fixYobJitter(true)
            .build();
    TaggedDatasetDto dataset = usSelectorService.generate(config);
    assertNotNull(dataset);
    List<RecordDto> records = dataset.getRecords();
    assertEquals(30, records.size());
    assertEquals(10, records.stream().filter(r -> r.getId().getSource().equals("A")).toList().size());
    assertEquals(20, records.stream().filter(r -> r.getId().getSource().equals("B")).toList().size());

    Map<String, List<RecordDto>> clusters = records.stream().collect(Collectors.groupingBy(r -> r.getId().getGlobal()));
    List<List<RecordDto>> duplicatePairs = clusters.values().stream().filter(v -> v.size() > 1).toList();
    assertEquals(5, duplicatePairs.size());

  }

  @Test
  void getNcvrChangeFilterLastname() {
    UsvrSelectionConfig config = UsvrSelectionConfig.builder()
            .clusterType(ClusterType.NC)
            .numRecordsA(20)
            .numRecordsB(20)
            .numDuplicates(5)
            .orderingSeed("abcd")
            .fixYobJitter(true)
            .changeFilter(UsvrSelectionConfig.ChangeFilter.builder()
                    .minChanges(1)
                    .changedAttributes(List.of("LASTNAME")).build())
            .build();
    TaggedDatasetDto dataset = usSelectorService.generate(config);
    assertNotNull(dataset);
    List<RecordDto> records = dataset.getRecords();
    assertEquals(40, records.size());
    assertEquals(20, records.stream().filter(r -> r.getId().getSource().equals("A")).toList().size());
    assertEquals(20, records.stream().filter(r -> r.getId().getSource().equals("B")).toList().size());

    Map<String, List<RecordDto>> clusters = records.stream().collect(Collectors.groupingBy(r -> r.getId().getGlobal()));
    List<List<RecordDto>> duplicatePairs = clusters.values().stream().filter(v -> v.size() > 1).toList();
    assertEquals(5, duplicatePairs.size());

    for (List<RecordDto> duplicatePair : duplicatePairs) {
      assertEquals(2, duplicatePair.size());
      RecordDto record0 = duplicatePair.get(0);
      RecordDto record1 = duplicatePair.get(1);
      Map<String, AttributeDto> attributes0 = record0.getAttributes();
      Map<String, AttributeDto> attributes1 = record1.getAttributes();
      assertNotEquals(attributes0.get("LASTNAME"), attributes1.get("LASTNAME"));
    }
  }

  @Test
  void getNcvrChangeFilter() {
    List<String> listOfChangedAttributes = List.of(
            "FIRSTNAME",
            "MIDDLENAME",
            "LASTNAME",
            "PLACEOFBIRTH",
            "SEX",
            "PLZ",
            "CITY"
    );
    UsvrSelectionConfig config = UsvrSelectionConfig.builder()
            .clusterType(ClusterType.NC)
            .numRecordsA(20)
            .numRecordsB(20)
            .numDuplicates(5)
            .orderingSeed("abcd")
            .fixYobJitter(true)
            .changeFilter(UsvrSelectionConfig.ChangeFilter.builder()
                    .minChanges(1)
                    .changedAttributes(listOfChangedAttributes)
                    .build())
            .build();
    TaggedDatasetDto dataset = usSelectorService.generate(config);
    assertNotNull(dataset);
    List<RecordDto> records = dataset.getRecords();
    assertEquals(40, records.size());
    assertEquals(20, records.stream().filter(r -> r.getId().getSource().equals("A")).toList().size());
    assertEquals(20, records.stream().filter(r -> r.getId().getSource().equals("B")).toList().size());

    Map<String, List<RecordDto>> clusters = records.stream().collect(Collectors.groupingBy(r -> r.getId().getGlobal()));
    List<List<RecordDto>> duplicatePairs = clusters.values().stream().filter(v -> v.size() > 1).toList();
    assertEquals(5, duplicatePairs.size());

    for (List<RecordDto> duplicatePair : duplicatePairs) {
      assertEquals(2, duplicatePair.size());
      RecordDto record0 = duplicatePair.get(0);
      RecordDto record1 = duplicatePair.get(1);
      Map<String, AttributeDto> attributes0 = record0.getAttributes();
      Map<String, AttributeDto> attributes1 = record1.getAttributes();
//      System.out.println(attributes0 + " " + attributes1);
      int agreeingAttributes = 0;
      int disagreeingAttributes = 0;
      for (Map.Entry<String, AttributeDto> entry : attributes0.entrySet()) {
        String attributeName = entry.getKey();
        if (!listOfChangedAttributes.contains(attributeName)) {
          continue;
        }
        AttributeDto attribute0 = entry.getValue();
        AttributeDto attribute1 = attributes1.get(attributeName);
//        System.out.println(attributeName + ": " + attribute0 + " " + attribute1);
        if (attribute0 == null && attribute1 == null) {
          agreeingAttributes++;
        } else {
          if (attribute1 != null) {
            if (attribute0.getValue().equals(attribute1.getValue())) {
              agreeingAttributes++;
            } else {
              disagreeingAttributes++;
            }
          }
        }
      }
      System.out.println(agreeingAttributes + " " + disagreeingAttributes);
      assertTrue(disagreeingAttributes >= 1);
//      assertTrue(disagreeingAttributes <= 3);
    }
  }

  @Test
  void getNcvrContentFilter() {
    UsvrSelectionConfig config = UsvrSelectionConfig.builder()
            .clusterType(ClusterType.NC)
            .numRecordsA(10)
            .numRecordsB(20)
            .numDuplicates(5)
            .orderingSeed("abcd")
            .fixYobJitter(true)
            .contentFilter(UsvrSelectionConfig.ContentFilter.builder()
                    .recordSelector(
                            new AttributeIsIn("SEX", List.of("FEMALE"))
                    )
                    .build())
            .build();
    TaggedDatasetDto dataset = usSelectorService.generate(config);
    assertNotNull(dataset);
    List<RecordDto> records = dataset.getRecords();
    assertEquals(30, records.size());
    assertEquals(10, records.stream().filter(r -> r.getId().getSource().equals("A")).toList().size());
    assertEquals(20, records.stream().filter(r -> r.getId().getSource().equals("B")).toList().size());

    assertTrue(records.stream().allMatch(r -> r.getAttributes()
            .getOrDefault("SEX", new AttributeDto()).getValue().equals("FEMALE")));
    assertFalse(records.stream().anyMatch(r -> r.getAttributes()
            .getOrDefault("SEX", new AttributeDto()).getValue().equals("MALE")));

    Map<String, List<RecordDto>> clusters = records.stream().collect(Collectors.groupingBy(r -> r.getId().getGlobal()));
    List<List<RecordDto>> duplicatePairs = clusters.values().stream().filter(v -> v.size() > 1).toList();
    assertEquals(5, duplicatePairs.size());
  }
}
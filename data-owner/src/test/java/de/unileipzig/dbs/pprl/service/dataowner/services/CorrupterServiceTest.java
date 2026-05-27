package de.unileipzig.dbs.pprl.service.dataowner.services;

import de.unileipzig.dbs.pprl.core.common.model.api.RecordId;
import de.unileipzig.dbs.pprl.core.common.model.impl.RecordIdComposed;
import de.unileipzig.dbs.pprl.core.common.monitoring.Tag;
import de.unileipzig.dbs.pprl.service.common.data.dto.DatasetDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordDto;
import de.unileipzig.dbs.pprl.service.common.services.AnalysisService;
import de.unileipzig.dbs.pprl.service.common.services.DatasetDtoService;
import de.unileipzig.dbs.pprl.service.common.services.DatasetMongoService;
import de.unileipzig.dbs.pprl.service.dataowner.config.CsvDatasetConfig;
import de.unileipzig.dbs.pprl.service.dataowner.data.dto.DatasetCorruptionRequestDto;
import de.unileipzig.dbs.pprl.service.dataowner.data.dto.DatasetGenerationConfigCreatorDto;
import de.unileipzig.dbs.pprl.service.dataowner.generator.DataSetGeneratorConfig;
import de.unileipzig.dbs.pprl.service.dataowner.modifier.DataSetModifierConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;


@Testcontainers
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class CorrupterServiceTest {

  @Container
  @ServiceConnection
  static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:8.0")
          .waitingFor(Wait.forListeningPort())
          .withStartupTimeout(Duration.ofSeconds(60));

  @Autowired
  CorrupterService corrupterService;

  @Autowired
  DatasetDtoService datasetDtoService;

  @Autowired
  AnalysisService analysisService;

  @Autowired
  DummyDataImportService dummyDataImportService;

  private long datasetId;

  @BeforeAll
  void beforeAll() throws IOException {
    String path = getClass().getClassLoader()
            .getResource("data/DS_Time.csv")
            .getPath();

    CsvDatasetConfig.SingleCsvDatasetConfig datasetConfig = new CsvDatasetConfig.SingleCsvDatasetConfig();
    datasetConfig.setLocation(path);
    datasetConfig.setSource("org");
    Optional<Long> optionalDatasetId = dummyDataImportService.importCsv(datasetConfig);
    assertTrue(optionalDatasetId.isPresent());
    datasetId = optionalDatasetId.get();
  }

  @Test
  void useConfigCreator() {
    DataSetGeneratorConfig config = corrupterService.getConfig(DatasetGenerationConfigCreatorDto.builder()
            .name("TEST")
            .referenceDatasetId(datasetId)
            .override(DataSetGeneratorConfig.builder()
                    .originalSize(20)
                    .modifiedSize(20)
                    .sourceOverlap(0.5)
                    .build()
            )
            .build()
    );
    assertEquals("TEST", config.getName());
    assertTrue(config.getConfigName().contains("TEST"));

    List<DataSetModifierConfig> duplicateModifierConfigs = config.getDuplicateModifierConfigs();
    assertEquals(5, duplicateModifierConfigs.size());
    assertEquals(4, duplicateModifierConfigs.stream().filter(DataSetModifierConfig::isTrueDuplicate).count());
    assertEquals(1, duplicateModifierConfigs.stream().filter(c -> !c.isTrueDuplicate()).count());
//    System.out.println(config);
  }

  @Test
  void corruptDataset() {
    List<RecordDto> allRecordsAsDto = datasetDtoService.getAllRecordsAsDto(datasetId);
    assertEquals(1000, allRecordsAsDto.size());

    DatasetCorruptionRequestDto request = DatasetCorruptionRequestDto.builder()
            .inputDatasetId(datasetId)
            .configCreator(DatasetGenerationConfigCreatorDto.builder()
                            .name("TEST")
                            .override(DataSetGeneratorConfig.builder()
                                            .originalSize(40)
                                            .modifiedSize(40)
                                            .sourceOverlap(0.5)
//                            .seed(23L)
                                            .build()
                            )
                            .build()
            )
            .build();
    long corruptedDatasetId = corrupterService.corruptDataset(request);

    List<RecordDto> records = datasetDtoService.getAllRecordsAsDto(corruptedDatasetId);
    assertEquals(80, records.size());
    assertEquals(40, records.stream().filter(r -> r.getId().getSource().equals("A")).toList().size());
    assertEquals(40, records.stream().filter(r -> r.getId().getSource().equals("B")).toList().size());

    // Check that each record has zero or one block id
    assertEquals(List.of(0, 1), records.stream().map(r -> r.getId().getBlocks().size()).distinct().sorted().toList());

    // Check that the singletons without modification have no block id:
    // 2 sources * (20 records - 5 modified records) = 30
    assertEquals(30, records.stream().map(r -> r.getId().getBlocks())
            .filter(blocks -> blocks.isEmpty()).count());
    // Check that the duplicates and the singletons with modification have a block id:
    // 2 sources * (20 duplicates + 5 modified records) = 50
    assertEquals(50, records.stream().map(r -> r.getId().getBlocks())
            .filter(blocks -> blocks.size() == 1).count());


    Map<String, List<RecordDto>> clusters = records.stream().collect(Collectors.groupingBy(r -> r.getId().getGlobal()));
    List<List<RecordDto>> duplicatePairs = clusters.values().stream().filter(v -> v.size() > 1).toList();
    assertEquals(20, duplicatePairs.size());
    for (List<RecordDto> duplicatePair : duplicatePairs) {
      assertEquals(1, duplicatePair.stream().map(r -> r.getId().getGlobal()).distinct().count());
      assertEquals(1, duplicatePair.stream().map(r -> r.getId().getBlocks().getFirst()).distinct().count());
    }

    Optional<DatasetDto> optionalDataset = datasetDtoService.getDataset(corruptedDatasetId);
    assertTrue(optionalDataset.isPresent());

    Collection<Tag> tags = analysisService.getTags(corruptedDatasetId, null);
    Map<String, List<Tag>> modifierTags = tags.stream()
            .filter(t -> t.getTag().equals("Modifier"))
            .collect(Collectors.groupingBy(Tag::getStringValue));

    List<String> expectedModifierTags = List.of("EXACT_DUPLICATE", "EMPTY_ADDRESS", "TYPO_FN_LN_CITY",
            "EXCHANGED_FIRST_LAST_NAME", "SIMILAR_BUT_OTHER_PLACE");

    for (String expectedModifierTag : expectedModifierTags) {
      assertTrue(modifierTags.containsKey(expectedModifierTag));
      List<Tag> currentTags = modifierTags.get(expectedModifierTag);
      // Expect 5 pairs per modifier because "SIMILAR_BUT_OTHER_PLACE" is a true non duplicate modifier,
      // which means: 20 duplicates / 4 true duplicate configs = 5 pair-tags per modifier group
      assertEquals(5, currentTags.size());

      // All modifier tags have a second id, because the describe a pair
      assertTrue(currentTags.stream().noneMatch(t -> t.getId1().isEmpty()));
    }
//    for (Tag tag : tags) {
//      System.out.println(tag);
//    }
  }
}
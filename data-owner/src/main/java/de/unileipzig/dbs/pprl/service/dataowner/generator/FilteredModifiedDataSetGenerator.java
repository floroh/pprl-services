package de.unileipzig.dbs.pprl.service.dataowner.generator;

import com.google.common.base.Functions;
import de.unileipzig.dbs.pprl.core.common.RecordUtils;
import de.unileipzig.dbs.pprl.core.common.comparators.RecordComparator;
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.DataSet;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordId;
import de.unileipzig.dbs.pprl.core.common.monitoring.TagTable;
import de.unileipzig.dbs.pprl.core.matcher.evaluation.GroundTruth;
import de.unileipzig.dbs.pprl.service.common.data.DefaultDataSet;
import de.unileipzig.dbs.pprl.service.dataowner.modifier.DataSetModifierConfig;
import de.unileipzig.dbs.pprl.service.dataowner.modifier.dataset.SingleSourceModifier;
import de.unileipzig.dbs.pprl.service.dataowner.modifier.dataset.TwoSourceModifier;
import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static de.unileipzig.dbs.pprl.service.dataowner.modifier.dataset.TwoSourceModifier.getRandomId;

/**
 * Create a two-party dataset based on a collection of input records without duplicates
 */
@Log4j2
public class FilteredModifiedDataSetGenerator {

  private final DataSetGeneratorConfig config;

  public FilteredModifiedDataSetGenerator(DataSetGeneratorConfig config) {
    this.config = config;
  }

  public DefaultDataSet generate(List<Record> inputRecords) {
    log.info("Start dataset generation");
    int orgSize = config.getOriginalSize();
    int orgOverlap = asInt(config.getOriginalSize() * config.getSourceOverlap());

    List<DataSetModifierConfig> duplicateModifierConfigs = config.getDuplicateModifierConfigs();
    long numberOfTrueDuplicateModifier = duplicateModifierConfigs.stream()
      .filter(DataSetModifierConfig::isTrueDuplicate).count();

    int chunkSize = switch (config.getModifierDistributionStrategy()) {
      case UNISIZE -> asInt((double) orgOverlap / numberOfTrueDuplicateModifier);
      case UNISIZE_RELAXED -> (int) Math.floor((double) orgOverlap / numberOfTrueDuplicateModifier);
    };
    int modifiedOrgSize = duplicateModifierConfigs.size() * chunkSize;
    int unmodifiedDupSize = config.getModifiedSize() - modifiedOrgSize;

    log.info("orgOverlap: {}, numberOfTrueDuplicateModifier: {}, chunkSize: {}, modifiedOrgSize: {}, unmodifiedDupSize: {}",
            orgOverlap, numberOfTrueDuplicateModifier, chunkSize, modifiedOrgSize, unmodifiedDupSize);

    if (config.getInputFilter() != null) {
      long preFilterSize = inputRecords.size();
      inputRecords = inputRecords.stream()
              .filter(config.getInputFilter())
              .collect(Collectors.toList());
      long postFilterSize = inputRecords.size();
      log.info("Removed records from input: " + (preFilterSize - postFilterSize) + "/" + preFilterSize);
    }

    if (inputRecords.size() < orgSize + unmodifiedDupSize) {
      throw new RuntimeException("Provided dataset is to small");
    }

    List<Record> preparedInputRecords = prepareInputRecords(inputRecords);

    // TODO Fix this workaround of naively using the first duplicate modifier
    String originalSourceName = config.getDuplicateModifierConfigs().getFirst().getOriginalSourceName();
    String modifiedSourceName = config.getDuplicateModifierConfigs().getFirst().getModifiedSourceName();
    RecordUtils.setId(preparedInputRecords, RecordId.SOURCE_ID, originalSourceName);

    Map<String, Record> inputRecordsByUniquelikeId = preparedInputRecords.stream().collect(Collectors.toMap(r -> r.getId().getUniqueLikeId(), Functions.identity()));


    TagTable tagTable = new TagTable();
    List<Record> modifiedRecords = new ArrayList<>();

    for (DataSetModifierConfig dataSetModifierConfig : duplicateModifierConfigs) {
      log.info("Applying modifier with tag: " + dataSetModifierConfig.getTag());
      Set<Record> currentRecordPool = new HashSet<>(inputRecordsByUniquelikeId.values());
      if (dataSetModifierConfig.getFilterRecordsToModify().isPresent()) {
        long preFilteredSize = currentRecordPool.size();
        currentRecordPool = currentRecordPool.stream()
          .filter(dataSetModifierConfig.getFilterRecordsToModify().get())
          .collect(Collectors.toSet());
        log.info("Remaining records: " + currentRecordPool.size() + "/" + preFilteredSize);
      }
      List<Record> currentRecordPoolList = new ArrayList<>(currentRecordPool);
      shuffleCollection(currentRecordPoolList);
      int curChunkSize = Math.min(chunkSize, currentRecordPoolList.size());
      if (curChunkSize < chunkSize) {
        log.warn("To few records left after filtering: " + curChunkSize + " < " + chunkSize);
      }
      List<Record> recordsInChunk = currentRecordPoolList.subList(0, curChunkSize);

      TwoSourceModifier generator = new TwoSourceModifier(dataSetModifierConfig);
      DataSet modifiedDataset = generator.generate(recordsInChunk);
      modifiedRecords.addAll(modifiedDataset.getAllRecords());
      recordsInChunk.forEach(r -> inputRecordsByUniquelikeId.remove(r.getId().getUniqueLikeId()));

      if (modifiedDataset.getTagTable().isPresent()) {
        TagTable curTagTable = modifiedDataset.getTagTable().get();
        if (tagTable == null) {
          tagTable = curTagTable;
        } else {
          tagTable.append(curTagTable);
        }
      }
    }
    List<Record> remainingRecords = new ArrayList<>(inputRecordsByUniquelikeId.values());

    // Select and prepare unmodified records
    List<Record> unmodifiedRecords = remainingRecords.subList(
      remainingRecords.size() - (orgSize + unmodifiedDupSize),
      remainingRecords.size()
    );
    for (Record unmodifiedRecord : unmodifiedRecords) {
      RecordId id = unmodifiedRecord.getId();
      id.addId("ORIGINAL_ID", id.getUniqueLikeId());
      unmodifiedRecord.setId(id);
    }
    // Prepare unmodified dup records
    List<Record> unmodifiedDupRecords = unmodifiedRecords.subList(0, unmodifiedDupSize);
    RecordUtils.setId(unmodifiedDupRecords, RecordId.SOURCE_ID, modifiedSourceName);

    // Select unmodified org records
    List<Record> recordsOrgAll = unmodifiedRecords.subList(unmodifiedDupSize, unmodifiedRecords.size());
    List<Record> unmodifiedOrgRecords = recordsOrgAll.subList(0, orgSize - modifiedOrgSize);
    RecordUtils.setId(unmodifiedOrgRecords, RecordId.SOURCE_ID, originalSourceName);

    List<Record> outputRecords = new ArrayList<>();
    outputRecords.addAll(unmodifiedOrgRecords);
    outputRecords.addAll(unmodifiedDupRecords);
    outputRecords.addAll(modifiedRecords);
    outputRecords.sort(new RecordComparator());


    log.info("Tag table size:" + tagTable.getTagList().size());

    outputRecords = outputRecords.stream()
            .peek(this::addUniqueGlobalIdIfMissing)
            .collect(Collectors.toList());

    GroundTruth gt = GroundTruth.createFromGlobalIds(outputRecords);

//    outputRecords = outputRecords.stream()
//      .peek(r -> r.getId().removeId(RecordId.GLOBAL_ID))
//      .peek(r -> r.getId().removeId(RecordId.BLOCK_ID))
//      .collect(Collectors.toList());

    DefaultDataSet datasetOut = new DefaultDataSet(outputRecords);
    if (tagTable != null) {

      datasetOut.setTagTable(tagTable);
    }
    datasetOut.setGroundTruth(gt);
    return datasetOut;
  }

  private void shuffleCollection(List<Record> currentRecordPoolList) {
    if (config.getSeed() != null && config.getSeed()!= 0) {
      Collections.shuffle(currentRecordPoolList, new Random(config.getSeed()));
    } else {
      Collections.shuffle(currentRecordPoolList);
    }
  }

  private void addUniqueGlobalIdIfMissing(Record input) {
    RecordId inputId = input.getId();
    if (inputId.getOptionalId(RecordId.GLOBAL_ID).isEmpty()) {
      String newInputGlobalId = getRandomId();
      inputId.addId(RecordId.LOCAL_ID, newInputGlobalId);
      inputId.addId(RecordId.GLOBAL_ID, newInputGlobalId);
    }
    input.setId(inputId);
  }

  /**
   * Preprocess input records, e.g. by filtering or manipulating them,
   * depending on the {@link SingleSourceModifier} that is used
   *
   * @param originalRecords only records from source org
   * @return
   */
  private List<Record> prepareInputRecords(List<Record> originalRecords) {
    SingleSourceModifier modifier = new SingleSourceModifier(
      config.getSourceModifierConfigs()
    );
    return modifier.modify(originalRecords);
  }

  private int asInt(double input) {
    if (isInt(input)) {
      return (int) input;
    }
    throw new NumberFormatException("Should be an integer: " + input);
  }

  private boolean isInt(double input) {
    return (input % 1) == 0;
  }

}

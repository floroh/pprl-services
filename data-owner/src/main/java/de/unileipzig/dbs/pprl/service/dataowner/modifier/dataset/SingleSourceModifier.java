package de.unileipzig.dbs.pprl.service.dataowner.modifier.dataset;

import de.unileipzig.dbs.pprl.core.common.model.api.DataSet;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordId;
import de.unileipzig.dbs.pprl.service.dataowner.modifier.DataSetModifierConfig;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Apply dataset modifications on records from a single source
 * The modified records are also assigned to this source.
 */
public class SingleSourceModifier implements DatasetModifier {

  List<DataSetModifierConfig> configs;

  public SingleSourceModifier(DataSetModifierConfig... configs) {
    this(List.of(configs));
  }

  public SingleSourceModifier(
    List<DataSetModifierConfig> configs) {
    this.configs = configs;
  }

  @Override
  public List<Record> modify(List<Record> records) {
    List<Record> output = records;
    for (DataSetModifierConfig config : configs) {
      output = modify(output, config);
    }
    return output;
  }

  private List<Record> modify(List<Record> originalRecords, DataSetModifierConfig config) {
    return config.getFilterRecordsToModify()
      .map(recordSelector -> applySingleSourceModification(originalRecords, config, recordSelector))
      .orElseGet(() -> applySingleSourceModification(originalRecords, config));
  }

  private static List<Record> applySingleSourceModification(List<Record> originalRecords,
    DataSetModifierConfig config) {
    TwoSourceModifier generator = new TwoSourceModifier(config);
    DataSet modifiedDataset = generator.generate(originalRecords);
    return modifiedDataset.getAllRecords().stream()
      .filter(r -> r.getId().getSourceId().equals(config.getModifiedSourceName()))
      .peek(r -> r.setId(r.getId().addId(RecordId.SOURCE_ID, config.getOriginalSourceName())))
      .collect(Collectors.toList());
  }

  private static List<Record> applySingleSourceModification(List<Record> originalRecords,
    DataSetModifierConfig config, Predicate<Record> filterRecordsToModify) {
    Map<Boolean, List<Record>> groups = originalRecords.stream()
      .collect(Collectors.partitioningBy(filterRecordsToModify));

    List<Record> outputRecords = applySingleSourceModification(groups.get(Boolean.TRUE), config);
    outputRecords.addAll(groups.get(Boolean.FALSE));
    return outputRecords;
  }
}

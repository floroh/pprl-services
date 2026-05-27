package de.unileipzig.dbs.pprl.service.dataowner.modifier.dataset;

import de.unileipzig.dbs.pprl.core.common.comparators.RecordComparator;
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.DataSet;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordId;
import de.unileipzig.dbs.pprl.core.common.monitoring.TagTable;
import de.unileipzig.dbs.pprl.service.common.data.DefaultDataSet;
import de.unileipzig.dbs.pprl.service.dataowner.modifier.DataSetModifierConfig;
import de.unileipzig.dbs.pprl.service.dataowner.modifier.attribute.SelectiveAttributeModifier;
import de.unileipzig.dbs.pprl.service.dataowner.modifier.record.SelectiveRecordModifier;
import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.stream.Collectors;

import static de.unileipzig.dbs.pprl.core.common.model.api.RecordId.BLOCK_ID;

/**
 * Creates a dataset that contains the original records as well as their modified counterparts
 */
@Log4j2
public class TwoSourceModifier {

  private DataSetModifierConfig config;

  public TwoSourceModifier(DataSetModifierConfig config) {
    this.config = config;
  }

  public DataSet generate(List<Record> inputs) {
    List<Record> clonedInputs = inputs.stream().map(Record::duplicate).collect(Collectors.toList());

    List<Record> outputRecords = new ArrayList<>(clonedInputs);
    TagTable tagTable = new TagTable();
    for (Record input : clonedInputs) {
      Record output = prepareInputAndOutputIds(input, config.isTrueDuplicate());
      String id0 = input.getId().getUniqueLikeId();
      String id1 = output.getId().getUniqueLikeId();
      for (Map.Entry<String, List<SelectiveAttributeModifier>> attributeModifier : config
        .getAttributeModifiers()
        .entrySet()) {
        String attributeName = attributeModifier.getKey();
        for (SelectiveAttributeModifier selectiveAttributeModifier : attributeModifier.getValue()) {
          if (selectiveAttributeModifier.getSelector().test(attributeName)) {
            Optional<Attribute> attribute = output.getAttribute(attributeName);
              if (attribute.isPresent()) {
                output.setAttribute(
                        attributeName,
                        selectiveAttributeModifier.getModifier().modifyToAttribute(
                                attribute.get().getAsString()
                        )
                );
              } else {
                log.info("Attribute " + attributeName + " not found. Skipping modification.");
                continue;
              }
            tagTable.addTag(id0, id1, attributeName, "Attribute-Modifier", selectiveAttributeModifier.getModifier().getTagPostFix(), null);
          }
        }
      }

      for (SelectiveRecordModifier recordModifier : config.getRecordModifiers()) {
        if (recordModifier.getSelector().test(input)) {
          output = recordModifier.getModifier().modify(output);
          for (String tag : recordModifier.getModifier().getTags()) {
            tagTable.addTag(id0, id1, tag);
          }
        }
      }
      if (config.getTag() != null) {
        tagTable.addTag(id0, id1, null, "Modifier", config.getTag(), null);
      }
      outputRecords.add(output);
    }
    outputRecords.sort(new RecordComparator());

    DataSet dataSet = new DefaultDataSet(outputRecords);
    dataSet.setTagTable(tagTable);

    return dataSet;
  }

  private Record prepareInputAndOutputIds(Record input, boolean isTrueDuplicate) {
    Record output = input.duplicate();

    RecordId inputId = input.getId();
    if (config.getOriginalSourceName() != null && !config.getOriginalSourceName().isEmpty()) {
      inputId.addId(RecordId.SOURCE_ID, config.getOriginalSourceName());
    }
    inputId.addId("ORIGINAL_ID", inputId.getUniqueLikeId());

    RecordId outputId = output.getId();
    outputId.addId(RecordId.SOURCE_ID, config.getModifiedSourceName());
    String blockId;

    if (isTrueDuplicate) {
      String newOutputGlobalId = getRandomId();
      inputId.addId(RecordId.LOCAL_ID, newOutputGlobalId);
      inputId.addId(RecordId.GLOBAL_ID, newOutputGlobalId);
      outputId.addId(RecordId.LOCAL_ID, newOutputGlobalId);
      outputId.addId(RecordId.GLOBAL_ID, newOutputGlobalId);
      blockId = newOutputGlobalId;
    } else {
      String newInputGlobalId = getRandomId();
      inputId.addId(RecordId.LOCAL_ID, newInputGlobalId);
      inputId.addId(RecordId.GLOBAL_ID, newInputGlobalId);
      String newOutputGlobalId = getRandomId();
      outputId.addId(RecordId.LOCAL_ID, newOutputGlobalId);
      outputId.addId(RecordId.GLOBAL_ID, newOutputGlobalId);
      blockId = newOutputGlobalId;
    }
    inputId.addId(BLOCK_ID, blockId);
    outputId.addId(BLOCK_ID, blockId);

    input.setId(inputId);
    output.setId(outputId);
    return output;
  }

  public static String getRandomId() {
    long l = Math.abs(UUID.randomUUID().getMostSignificantBits());
    return Long.toString(l);
  }
}

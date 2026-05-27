package de.unileipzig.dbs.pprl.service.dataowner.modifier;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.service.dataowner.modifier.attribute.SelectiveAttributeModifier;
import de.unileipzig.dbs.pprl.service.dataowner.modifier.record.SelectiveRecordModifier;
import de.unileipzig.dbs.pprl.core.common.selector.Selector;
import lombok.*;

import java.util.*;

@Data
@Builder
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property = "@class")
@AllArgsConstructor
public class DataSetModifierConfig {

  private String tag;

  @Builder.Default
  private String originalSourceName = "A";

  @Builder.Default
  private String modifiedSourceName = "B";

  @Builder.Default
  private boolean isTrueDuplicate = true;

  private Selector<Record> filterRecordsToModify;

  @Singular
  private List<SelectiveRecordModifier> recordModifiers;

  @Singular
  private Map<String, List<SelectiveAttributeModifier>> attributeModifiers;

  public DataSetModifierConfig() {
    recordModifiers = new ArrayList<>();
    attributeModifiers = new HashMap<>();
  }

  public Optional<Selector<Record>> getFilterRecordsToModify() {
    return Optional.ofNullable(filterRecordsToModify);
  }

  public static String serialize(DataSetModifierConfig config) throws JsonProcessingException {
    final ObjectMapper om = new ObjectMapper();
    om.registerModule(new Jdk8Module());
    return om.writerWithDefaultPrettyPrinter().writeValueAsString(config);
  }

  public static DataSetModifierConfig deserialize(String jsonString) throws JsonProcessingException {
    final ObjectMapper om = new ObjectMapper();
    om.registerModule(new Jdk8Module());
    return om.readValue(jsonString, DataSetModifierConfig.class);
  }
}

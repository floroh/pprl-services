package de.unileipzig.dbs.pprl.service.generator.services;

import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.impl.PersonalAttributeType;
import de.unileipzig.dbs.pprl.core.common.selector.AttributeIsIn;
import de.unileipzig.dbs.pprl.core.common.selector.SelectorCombination;
import de.unileipzig.dbs.pprl.service.generator.data.dto.UsvrSelectionConfig;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class ExampleProviderService {

  public UsvrSelectionConfig createFullExampleConfig() {
    return UsvrSelectionConfig.builder()
            .orderingSeed("alpha42")
            .attributeColumns(Arrays.asList(
                    PersonalAttributeType.FIRSTNAME.asString(),
                    PersonalAttributeType.LASTNAME.asString(),
                    PersonalAttributeType.YEAROFBIRTH.asString()
            ))
            .timeFilter(UsvrSelectionConfig.TimeFilter.builder()
                    .minDays(365).maxDays(700).build())
            .changeFilter(UsvrSelectionConfig.ChangeFilter.builder()
                    .minChanges(1).build())
            .build();
  }

  public UsvrSelectionConfig createTimeExampleConfig() {
    return UsvrSelectionConfig.builder()
            .orderingSeed("alpha42")
            .timeFilter(UsvrSelectionConfig.TimeFilter.builder()
                    .minDays(365).maxDays(700).build())
            .build();
  }

  public UsvrSelectionConfig createChangesExampleConfig() {
    return UsvrSelectionConfig.builder()
            .orderingSeed("alpha42")
            .changeFilter(UsvrSelectionConfig.ChangeFilter.builder()
                    .minChanges(1).build())
            .build();
  }

  public UsvrSelectionConfig createContentFilterExampleConfig() {
    SelectorCombination<Record> selector = new SelectorCombination<>(
            SelectorCombination.Operation.AND,
            new AttributeIsIn(PersonalAttributeType.SEX.asString(), List.of("FEMALE"))
    );

    UsvrSelectionConfig.ContentFilter contentFilter = new UsvrSelectionConfig.ContentFilter();
    contentFilter.setRecordSelector(selector);
    return UsvrSelectionConfig.builder()
            .orderingSeed("alpha42")
            .contentFilter(contentFilter)
            .build();
  }

  public UsvrSelectionConfig createDefaultExampleConfig() {
    return UsvrSelectionConfig.builder()
            .orderingSeed("alpha42")
            .build();
  }
}

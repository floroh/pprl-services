package de.unileipzig.dbs.pprl.service.generator.selection.model.converter;

import de.unileipzig.dbs.pprl.core.common.serialization.AttributeSerializationType;
import de.unileipzig.dbs.pprl.service.common.data.dto.AttributeDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordIdDto;
import de.unileipzig.dbs.pprl.service.generator.selection.model.common.RecordOutput;

import java.util.Map;
import java.util.stream.Collectors;

public class SelectionRecordConverter {

  public static RecordDto toRecordDto(RecordOutput recordOutput) {
    return RecordDto.builder()
            .id(RecordIdDto.builder()
                    .local(recordOutput.getId())
                    .source(recordOutput.getParty())
                    .global(recordOutput.getId())
                    .build())
            .attributes(recordOutput.getRecord().getAttributes().entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> AttributeDto.builder()
                                    .type(AttributeSerializationType.STRING.name())
                                    .value(e.getValue())
                                    .build()
                    )))
            .build();
  }
}

package de.unileipzig.dbs.pprl.service.common.data.converter;

import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordDto;

public class RecordConverter extends AbstractRecordConverter<RecordDto> {

  @Override
  public Record toRecord(RecordDto dto) {
    Record record = emptyRecord(dto.getId());
    dto.getAttributes().forEach(
      (k, v) -> record.setAttribute(k, AttributeConverter.fromDto(v))
    );
    return record;
  }

  @Override
  public RecordDto fromRecord(Record record) {
    RecordDto.RecordDtoBuilder dtoBuilder = RecordDto.builder()
      .id(fromRecordId(record.getId()));
    record.getAttributes().forEach(
      (k, v) -> dtoBuilder.attribute(k, AttributeConverter.toDto(v))
    );
    return dtoBuilder.build();
  }

}

package de.unileipzig.dbs.pprl.service.common.data.converter;

import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordIdDto;
import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoRecord;

public class MongoRecordConverter extends AbstractRecordConverter<RecordDto> {

  @Override
  public Record toRecord(RecordDto dto) {
    MongoRecord record = emptyRecord(dto.getDatasetId(), dto.getId());
    if (dto.getEncodingId() != null) {
      record.setEncodingId(dto.getEncodingId());
    }

    dto.getAttributes().forEach(
      (k, v) -> record.setAttribute(k, AttributeConverter.fromDto(v))
    );
    return record;
  }

  @Override
  public RecordDto fromRecord(Record record) {
    RecordDto.RecordDtoBuilder dtoBuilder = RecordDto.builder()
      .id(fromRecordId(record.getId()));
    if (record instanceof MongoRecord) {
      dtoBuilder.encodingId(((MongoRecord) record).getEncodingId());
    }
    record.getAttributes().forEach(
      (k, v) -> dtoBuilder.attribute(k, AttributeConverter.toDto(v))
    );
    if (record instanceof MongoRecord) {
      dtoBuilder.datasetId(((MongoRecord) record).getIdDataset());
    }
    RecordDto dto = dtoBuilder.build();
    if (record instanceof MongoRecord) {
      if (((MongoRecord)record).getObjectId() != null) {
        dto.getId().setUnique(((MongoRecord)record).getObjectId().toHexString());
      }
    }
    return dto;
  }

  private MongoRecord emptyRecord(int datasetId, RecordIdDto idDto) {
    return new MongoRecord(datasetId, toRecordId(idDto));
  }

}

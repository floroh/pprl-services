package de.unileipzig.dbs.pprl.service.common.data.converter;

import de.unileipzig.dbs.pprl.core.common.factories.RecordFactory;
import de.unileipzig.dbs.pprl.core.common.factories.RecordIdFactory;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordId;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordIdDto;

import java.util.List;
import java.util.Optional;

public abstract class AbstractRecordConverter<T> {

  public abstract Record toRecord(T dto);

  public abstract T fromRecord(Record record);

  public static Record emptyRecord(RecordIdDto id) {
    Optional<RecordIdDto> optionalId = Optional.ofNullable(id);
    return optionalId.map(s -> RecordFactory.getEmptyRecord(toRecordId(id)))
      .orElseGet(RecordFactory::getEmptyRecord);
  }

  public static RecordId toRecordId(RecordIdDto idDto) {
    RecordId id = RecordIdFactory.get(idDto.getLocal());
    id.addId(RecordId.SOURCE_ID, idDto.getSource());
    id.addId(RecordId.GLOBAL_ID, idDto.getGlobal());
    id.addId(RecordId.UNIQUE_ID, idDto.getUnique());
    List<String> blocks = idDto.getBlocks();
    if (blocks != null) {
      //TODO Improve handling of multiple BLOCK_IDs
      if (blocks.size() == 1) {
        id.addId(RecordId.BLOCK_ID, blocks.getFirst());
      } else {
        for (int i = 0; i < blocks.size(); i++) {
          id.addId(RecordId.BLOCK_ID + "_" + i, blocks.get(i));
        }
      }
    }
    return id;
  }

  public static RecordIdDto fromRecordId(RecordId id) {
    RecordIdDto.RecordIdDtoBuilder builder = RecordIdDto.builder()
      .local(id.getLocalId())
      .source(id.getSourceId());
    id.getOptionalId(RecordId.GLOBAL_ID).ifPresent(builder::global);
    id.getOptionalId(RecordId.BLOCK_ID).ifPresent(builder::block);
    if (id.getOptionalId(RecordId.BLOCK_ID + "_0").isPresent()) {
      int i = 0;
      while (id.getOptionalId(RecordId.BLOCK_ID + "_" + i).isPresent()) {
        builder.block(id.getOptionalId(RecordId.BLOCK_ID + "_" + i).get());
        i++;
      }
    }
    return builder.build();
  }

}

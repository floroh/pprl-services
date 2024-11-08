package de.unileipzig.dbs.pprl.service.common.data.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EncodedDatasetDto {

  @NonNull
  private List<RecordDto> records;

  @NonNull
  private EncodingIdDto encodingId;

}

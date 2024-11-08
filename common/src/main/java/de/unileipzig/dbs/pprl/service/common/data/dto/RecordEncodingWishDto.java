package de.unileipzig.dbs.pprl.service.common.data.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import de.unileipzig.dbs.pprl.service.common.data.dto.EncodingIdDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordIdDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordEncodingWishDto {

  @NonNull
  @JsonUnwrapped(prefix = "id.")
  private RecordIdDto id;

  @NonNull
  private EncodingIdDto encodingId;

  private String recordSecret;

  private long orderId;

}

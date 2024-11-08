package de.unileipzig.dbs.pprl.service.dataowner.data.dto;

import de.unileipzig.dbs.pprl.service.common.data.dto.EncodingIdDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * Request for encoding multiple records using the same method.
 * The records are included in the request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request for encoding multiple records using the same method. " +
  "The records are included in the request.")
public class MultiRecordEncodingRequestDto {

  @NonNull
  private EncodingIdDto encodingId;

  @NonNull
  private List<RecordDto> records;


}

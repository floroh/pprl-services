package de.unileipzig.dbs.pprl.service.dataowner.data.dto;

import de.unileipzig.dbs.pprl.service.common.data.dto.EncodingIdDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;


/**
 * Request for encoding a single record that is included in the request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request for encoding a single record that is included in the request.")
public class EncodingRequestDto {

  @NonNull
  @NotBlank(message = "Encoding id is mandatory")
  private EncodingIdDto encodingId;

  private RecordDto record;

  private String recordSecret;
}

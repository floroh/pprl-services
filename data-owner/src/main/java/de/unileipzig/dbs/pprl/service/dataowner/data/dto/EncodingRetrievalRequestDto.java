package de.unileipzig.dbs.pprl.service.dataowner.data.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import de.unileipzig.dbs.pprl.service.common.data.dto.EncodingIdDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordIdDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Request description for encoding a single record.
 * The record itself is not part of the request, but retrieved from the database using its id.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request description for encoding a single record." +
  " The record itself is not part of the request, but retrieved from the database using its id.")
public class EncodingRetrievalRequestDto {

  @NonNull
  @NotBlank(message = "Encoding id is mandatory")
  private EncodingIdDto encodingId;

  @JsonUnwrapped(prefix = "id.")
  private RecordIdDto recordId;

  private int datasetId;

  private String recordSecret;
}

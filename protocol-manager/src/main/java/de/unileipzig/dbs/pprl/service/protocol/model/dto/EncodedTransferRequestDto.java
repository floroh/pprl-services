package de.unileipzig.dbs.pprl.service.protocol.model.dto;

import de.unileipzig.dbs.pprl.service.common.data.dto.EncodingIdDto;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EncodedTransferRequestDto {

  private int dataOwnerDatasetId;

  @NonNull
  @NotBlank(message = "Encoding id is mandatory")
  private EncodingIdDto encoding;
}

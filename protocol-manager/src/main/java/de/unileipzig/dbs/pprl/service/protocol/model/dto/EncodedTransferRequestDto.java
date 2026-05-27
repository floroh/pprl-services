package de.unileipzig.dbs.pprl.service.protocol.model.dto;

import de.unileipzig.dbs.pprl.service.common.data.dto.EncodingIdDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EncodedTransferRequestDto {

  @NotNull
  private Long dataOwnerDatasetId;

  private Long linkageUnitDatasetId;

  @NotNull
  @NotBlank(message = "Encoding id is mandatory")
  private EncodingIdDto encoding;
}

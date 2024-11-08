package de.unileipzig.dbs.pprl.service.common.data.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Schema(description = "Description of an encoding scheme")
public class EncodingDto {

  @NonNull
  @Schema(description = "ID of this encoding scheme")
  private EncodingIdDto id;

  @Schema(description = "Description of requirements for this scheme")
  private RecordRequirementsDto validation;

  @Schema(description = "Configuration / parameters to build the encoder")
  private String config;

}

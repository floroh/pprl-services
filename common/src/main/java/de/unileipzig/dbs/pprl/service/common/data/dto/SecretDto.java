package de.unileipzig.dbs.pprl.service.common.data.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;


@Data
@NoArgsConstructor
@Schema(description = "Project-specific secret necessary to create unique encodings per project, " +
  "that is is only known to the data owners")
public class SecretDto {

  @NonNull
  @NotBlank(message = "Project name is mandatory")
  @Schema(description = "Unique name of the record-linkage project")
  private String project;

  @NonNull
  @NotBlank(message = "Secret value is mandatory")
  @Schema(description = "Secret value")
  private String secret;

}

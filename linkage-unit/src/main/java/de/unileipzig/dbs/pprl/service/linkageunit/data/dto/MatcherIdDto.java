package de.unileipzig.dbs.pprl.service.linkageunit.data.dto;

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
@Schema(description = "ID of an matching scheme")
public class MatcherIdDto {

  @NonNull
  @Schema(description = "Name of the matching scheme")
  private String method;

  @Schema(description = "(Optional) Project name where this scheme is used")
  private String project;

}

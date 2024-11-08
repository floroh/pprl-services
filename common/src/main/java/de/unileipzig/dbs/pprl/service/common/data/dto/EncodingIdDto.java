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
@Schema(description = "ID of an encoding scheme")
public class EncodingIdDto {

  @NonNull
  @Schema(description = "Name of the encoding scheme")
  private String method;

  @Schema(description = "Unique name of the record-linkage project")
  private String project;

  public boolean isSubtypeOf(EncodingIdDto otherId) {
    if (!method.equals(otherId.getMethod())) {
      return false;
    } else {
      if (otherId.getProject() == null) {
        return true;
      }
      return otherId.getProject().equals(project);
    }
  }
}

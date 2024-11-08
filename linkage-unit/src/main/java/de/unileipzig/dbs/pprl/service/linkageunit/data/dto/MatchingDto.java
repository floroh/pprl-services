package de.unileipzig.dbs.pprl.service.linkageunit.data.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordRequirementsDto;
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
@Schema(description = "Description of a matching scheme")
public class MatchingDto {

  @NonNull
  @Schema(description = "Name of this matching scheme")
  private MatcherIdDto id;

  @Schema(description = "Description of requirements for this scheme")
  private RecordRequirementsDto validation;

  @Schema(description = "Configuration / parameters to build the matcher")
  private String config;

  @Schema(description = "Description of the classifier")
  private String classifierDescription;

}

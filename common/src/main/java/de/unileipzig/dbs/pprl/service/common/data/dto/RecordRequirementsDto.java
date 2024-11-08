package de.unileipzig.dbs.pprl.service.common.data.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class RecordRequirementsDto {

  private String method;

  @Schema(description = "List of encoding methods that can be matched with this scheme")
  private List<String> supportedEncodingMethods;

  @Singular
  private List<AttributeDescriptionDto> attributes;

}

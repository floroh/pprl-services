package de.unileipzig.dbs.pprl.service.linkageunit.data.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.unileipzig.dbs.pprl.service.linkageunit.data.mongo.ProjectState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BatchMatchProjectExecutionDto {

  private String projectId;

  private ProjectState fromState;

  private ProjectState toState;

}

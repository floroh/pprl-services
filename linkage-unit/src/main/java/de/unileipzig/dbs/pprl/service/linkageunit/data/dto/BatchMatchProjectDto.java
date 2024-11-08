package de.unileipzig.dbs.pprl.service.linkageunit.data.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.unileipzig.dbs.pprl.service.linkageunit.data.mongo.BatchMatchProjectPhase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BatchMatchProjectDto {

  private String projectId;

  /**
   * Timestamp of last change of the project
   */
  private String lastUpdate;

  /**
   * Name of the matcher that must match the method name in the
   * {@link de.unileipzig.dbs.pprl.service.linkageunit.data.dto.MatcherIdDto}
   */
  private String method;

  private String description;

  private int datasetId;

  /**
   * Determine if the project should be run fully automated or allows human interaction
   */
  private boolean interactive;

  @Singular
  private Map<String, String> configs;

  private String currentState;

  private Map<String, BatchMatchProjectPhase> phases;
}

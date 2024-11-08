package de.unileipzig.dbs.pprl.service.linkageunit.data.mongo;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Data
@Document
public class BatchMatchProject {

  @Id
  private ObjectId projectId;

  private String lastUpdate;

  private String method;

  private String description;

  private int datasetId;

  private ProjectState state = ProjectState.COLLECTING;

  /**
   * True, if the processing of this project should stop after each phase
   * and the next phase must be manually triggered
   */
  private boolean interactive;

  /**
   * Settings for the project, e.g., if certain reports should be generated
   */
  private Map<String, String> config = new HashMap<>();

  private Map<String, BatchMatchProjectPhase> phases = new HashMap<>();

  private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  public BatchMatchProject setPhase(String name, BatchMatchProjectPhase phase) {
    phases.put(name, phase);
    return this;
  }

  public void updateLastUpdateToCurrentTime() {
    lastUpdate = sdf.format(System.currentTimeMillis());
  }

  public Optional<String> getConfigValue(String key) {
    return Optional.ofNullable(config.get(key));
  }
}

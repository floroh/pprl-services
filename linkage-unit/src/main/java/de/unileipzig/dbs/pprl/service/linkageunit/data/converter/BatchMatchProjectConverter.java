package de.unileipzig.dbs.pprl.service.linkageunit.data.converter;

import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.BatchMatchProjectDto;
import de.unileipzig.dbs.pprl.service.linkageunit.data.mongo.BatchMatchProject;
import org.bson.types.ObjectId;

public class BatchMatchProjectConverter {

  public static BatchMatchProjectDto projectToDto(BatchMatchProject batchMatchProject) {
    BatchMatchProjectDto.BatchMatchProjectDtoBuilder builder = BatchMatchProjectDto.builder()
      .projectId(batchMatchProject.getProjectId().toHexString())
      .lastUpdate(batchMatchProject.getLastUpdate())
      .method(batchMatchProject.getMethod())
      .description(batchMatchProject.getDescription())
      .datasetId(batchMatchProject.getDatasetId())
      .interactive(batchMatchProject.isInteractive())
      .currentState(batchMatchProject.getState().toString())
      .phases(batchMatchProject.getPhases());

    if (batchMatchProject.getConfig() != null) {
      batchMatchProject.getConfig().forEach(builder::config);
    }
    return builder.build();
  }

  public static BatchMatchProject dtoToProject(BatchMatchProjectDto dto) {
    BatchMatchProject batchMatchProject = new BatchMatchProject();
    if (dto.getProjectId() != null) {
      batchMatchProject.setProjectId(new ObjectId(dto.getProjectId()));
    }
    if (dto.getLastUpdate() != null) {
      batchMatchProject.setLastUpdate(dto.getLastUpdate());
    }
    batchMatchProject.setMethod(dto.getMethod());
    batchMatchProject.setDescription(dto.getDescription());
    batchMatchProject.setDatasetId(dto.getDatasetId());
    batchMatchProject.setInteractive(dto.isInteractive());
    if (dto.getConfigs() != null) {
      dto.getConfigs().forEach((k,v) -> batchMatchProject.getConfig().put(k, v));
    }
    return batchMatchProject;
  }

//  public static BatchMatchProjectPhaseDto phaseToDto(BatchMatchProjectPhase phase) {
//    return BatchMatchProjectPhaseDto.builder()
//      .name(phase.getName())
//      .progress(phase.getPhaseProgress().getProgress())
//      .isDone(phase.getPhaseProgress().isDone())
//      .reportGroups(phase.getReportGroups())
//      .build();
//  }
}

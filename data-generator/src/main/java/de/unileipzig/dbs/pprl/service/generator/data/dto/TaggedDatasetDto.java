package de.unileipzig.dbs.pprl.service.generator.data.dto;

import de.unileipzig.dbs.pprl.core.common.monitoring.Tag;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaggedDatasetDto {

  private int datasetId;
  private List<RecordDto> records;
  private List<Tag> tags;
}

package de.unileipzig.dbs.pprl.service.linkageunit.data.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.unileipzig.dbs.pprl.core.common.monitoring.Tag;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordIdDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordIdPairDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
@RequiredArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecordPairDto {

  @NonNull
  private RecordIdDto id0;

  @NonNull
  private RecordIdDto id1;

  private String projectId;

  private String matchGrade;

  private Double similarity;

  private Set<String> properties;

  private Map<String, Double> attributeSimilarities;

  private List<Tag> tags;

  public RecordIdPairDto asRecordIdPairDto() {
    return RecordIdPairDto.builder()
      .leftRecordId(id0)
      .rightRecordId(id1)
      .build();
  }
}

package de.unileipzig.dbs.pprl.service.linkageunit.data.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.unileipzig.dbs.pprl.core.common.monitoring.Tag;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordIdPairDto;
import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
@RequiredArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecordPairWithRecordsDto {

  @NonNull
  private RecordDto record0;

  @NonNull
  private RecordDto record1;

  private String projectId;

  private String matchGrade;

  private Double similarity;

  private Set<String> properties;

  private Map<String, Double> attributeSimilarities;

  private List<Tag> tags;

  public RecordIdPairDto asRecordIdPairDto() {
    return RecordIdPairDto.builder()
      .leftRecordId(record0.getId())
      .rightRecordId(record1.getId())
      .build();
  }
}

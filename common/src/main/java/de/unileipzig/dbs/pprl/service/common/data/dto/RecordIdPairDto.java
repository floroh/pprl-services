package de.unileipzig.dbs.pprl.service.common.data.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.unileipzig.dbs.pprl.core.common.RecordUtils;
import de.unileipzig.dbs.pprl.core.matcher.classification.Classifier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RecordIdPairDto {

  private RecordIdDto leftRecordId;

  private RecordIdDto rightRecordId;

  private Classifier.Label label;

  @JsonIgnore
  public String getUniqueLikePairId() {
    return RecordUtils.getPairId(leftRecordId.getUniqueLike(), rightRecordId.getUniqueLike());
  }
}


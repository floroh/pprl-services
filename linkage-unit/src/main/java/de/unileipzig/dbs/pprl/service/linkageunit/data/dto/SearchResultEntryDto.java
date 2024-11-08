package de.unileipzig.dbs.pprl.service.linkageunit.data.dto;

import de.unileipzig.dbs.pprl.service.common.data.dto.RecordIdDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultEntryDto {

  private RecordIdDto foundId;

  private Double similarity;

}

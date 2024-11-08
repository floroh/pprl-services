package de.unileipzig.dbs.pprl.service.linkageunit.data.dto;

import de.unileipzig.dbs.pprl.service.common.data.dto.RecordIdDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Singular;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class SearchResultDto {

  @NonNull
  private RecordIdDto queryId;

  @Singular
  private List<SearchResultEntryDto> matches;

}

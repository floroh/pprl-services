package de.unileipzig.dbs.pprl.service.linkageunit.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComparingRequestDto {

  private Collection<RecordPairWithRecordsDto> recordPairs;

  private String method;

}

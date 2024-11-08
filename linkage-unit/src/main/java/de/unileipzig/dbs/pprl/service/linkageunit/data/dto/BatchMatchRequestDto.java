package de.unileipzig.dbs.pprl.service.linkageunit.data.dto;

import de.unileipzig.dbs.pprl.service.common.data.dto.RecordDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchMatchRequestDto {

  private Collection<RecordDto> records;

  private String method;

}

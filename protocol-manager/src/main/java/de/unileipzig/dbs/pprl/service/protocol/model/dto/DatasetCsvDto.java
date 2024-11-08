package de.unileipzig.dbs.pprl.service.protocol.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatasetCsvDto {

  private String path;

  private int datasetId;

}

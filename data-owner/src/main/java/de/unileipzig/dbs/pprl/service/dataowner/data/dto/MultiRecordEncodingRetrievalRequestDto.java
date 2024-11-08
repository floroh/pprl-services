package de.unileipzig.dbs.pprl.service.dataowner.data.dto;

import de.unileipzig.dbs.pprl.service.common.data.dto.EncodingIdDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordIdDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;

/**
 * Request description for encoding multiple records from the same dataset with the same encoding.
 * The records are not part of the request, but are defined by the dataset id and the record ids.
 * If no record ids are given, all records of the dataset are encoded.
 * No record-specific secret is required or possible.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request description for encoding multiple records from the same dataset " +
  "with the same encoding. No record-specific secret is required or possible.")
public class MultiRecordEncodingRetrievalRequestDto {

  @NonNull
  private EncodingIdDto encodingId;

  @Schema(description = "List of record ids to encode. If empty, all records of the dataset are encoded.")
  private List<RecordIdDto> recordIds;

  private int datasetId;


}

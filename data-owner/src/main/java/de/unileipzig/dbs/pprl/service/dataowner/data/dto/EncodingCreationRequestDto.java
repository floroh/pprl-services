package de.unileipzig.dbs.pprl.service.dataowner.data.dto;

import de.unileipzig.dbs.pprl.service.common.data.dto.EncodingIdDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;
import java.util.Map;


/**
 * Request for creating an encoding definition.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request for creating an encoding definition.")
public class EncodingCreationRequestDto {

  @Builder.Default
  private WeightSelectionMethod weightSelectionMethod = WeightSelectionMethod.NONE;

  @Builder.Default
  private FrequencySelectionMethod frequencySelectionMethod = FrequencySelectionMethod.NONE;

  @Builder.Default
  private EncodingCreationMethod encodingCreationMethod = EncodingCreationMethod.BASE_ENCODING_ADAPTION;

  private EncodingIdDto baseEncodingId;

  private EncodingIdDto outputEncodingId;

  private Map<String, Double> attributeWeights;

  private Map<String, Double> attributeLength;

  @Builder.Default
  private boolean sourceSpecific = false;

  @Schema(description = "Precomputed/estimated error rates." +
          "Required if sourceSpecific = true because the error rate cannot be determined per source.")
  private Map<String, Double> attributeErrorRates;

  @Schema(description = "Attribute weights for each data source. Only relevant if sourceSpecific = true.")
  private Map<String, Map<String, Double>> sourceSpecificAttributeWeights;

  @Schema(description = "Attribute lengths for each data source. Only relevant if sourceSpecific = true.")
  private Map<String, Map<String, Double>> sourceSpecificAttributeLength;

  @Schema(description = "Names of sources. Only relevant for sourceSpecific = true and WeightSelectionMethod.None.")
  private List<String> sources;

  private Long datasetId;

  private float averageFillrate;

  @Builder.Default
  private float maxFillrate = 0.5f;

  @Builder.Default
  private int bloomFilterSize = 1024;

  @Builder.Default
  private boolean persist = true;

  public enum WeightSelectionMethod {
    NONE,
    FELLEGI_SUNTER,
  }

  public enum FrequencySelectionMethod {
    NONE,
    DATABASE,
  }

  public enum EncodingCreationMethod {
    BASE_ENCODING_ADAPTION,
    RBF,
    ABF
  }
}

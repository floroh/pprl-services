package de.unileipzig.dbs.pprl.service.dataowner.controller;

import de.unileipzig.dbs.pprl.service.common.data.dto.EncodingDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.EncodingIdDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordRequirementsDto;
import de.unileipzig.dbs.pprl.service.common.utils.LocalConfigHandlerUtils;
import de.unileipzig.dbs.pprl.service.dataowner.services.EncoderProviderService;
import de.unileipzig.dbs.pprl.service.dataowner.services.EncoderService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Timed
@Tag(name = ConfigController.TAG, description = "Manage encoding configurations")
@RequestMapping(value = "/config", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin
public class ConfigController {

  public static final String TAG = "Configuration management";

  private final EncoderProviderService encoderProviderService;

  private final EncoderService encoderService;

  public ConfigController(EncoderProviderService encoderProviderService,
    EncoderService encoderService) {
    this.encoderProviderService = encoderProviderService;
    this.encoderService = encoderService;
  }

  @Operation(summary = "Add a configuration", tags = ConfigController.TAG)
  @PostMapping("/")
  public void add(@RequestBody EncodingDto encoding) {
    encoderProviderService.addEncoding(encoding);
  }

  @Operation(summary = "Override an existing configuration", tags = ConfigController.TAG)
  @PutMapping("/")
  public void update(@RequestBody EncodingDto encoding) {
    encoderProviderService.updateEncoding(encoding);
  }

  @Operation(summary = "Remove a configuration", tags = ConfigController.TAG)
  @DeleteMapping("/")
  public void remove(@RequestBody EncodingIdDto encodingId) {
    encoderProviderService.removeEncoding(encodingId);
    encoderService.removeEncoder(encodingId);
  }


  @Operation(summary = "Get the configuration by its ID", tags = ConfigController.TAG)
  @PostMapping("/findById")
  public EncodingDto getEncoding(@RequestBody EncodingIdDto idDto) {
    List<EncodingDto> selected = encoderProviderService.getAvailableEncodings().stream()
      .filter(enc -> enc.getId().equals(idDto))
      .collect(Collectors.toList());
    if (selected.isEmpty()) {
      throw new RuntimeException("No encoding found for " + idDto);
    }
    return selected.getFirst();
  }

  @Operation(summary = "Get a list of all configuration IDs", tags = ConfigController.TAG)
  @GetMapping("/findAll")
  public List<EncodingIdDto> getConfigs() {
    return encoderProviderService.getAvailableEncodings().stream()
      .map(EncodingDto::getId)
      .collect(Collectors.toList());
  }

  @Operation(summary = "Get descriptions of the requirements of all encoding methods", tags =
    ConfigController.TAG)
  @GetMapping("/findAllRequirements")
  public List<RecordRequirementsDto> getEncodingMethodRequirements() {
    return encoderProviderService.getAvailableEncodings().stream()
      .map(encodingDto -> {
        RecordRequirementsDto validation = encodingDto.getValidation();
        if (validation != null) {
          validation.setMethod(encodingDto.getId().getMethod());
          LocalConfigHandlerUtils.addDisplayNames(validation);
        }
        return validation;
      })
      .collect(Collectors.toList());
  }

}
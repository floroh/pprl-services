package de.unileipzig.dbs.pprl.service.protocol.controller;

import de.unileipzig.dbs.pprl.service.protocol.model.dto.EncodedTransferRequestDto;
import de.unileipzig.dbs.pprl.service.protocol.model.dto.ProtocolExecutionDto;
import de.unileipzig.dbs.pprl.service.protocol.model.mongo.MultiLayerProtocol;
import de.unileipzig.dbs.pprl.service.protocol.service.MultiLayerProtocolCreator;
import de.unileipzig.dbs.pprl.service.protocol.service.ProtocolService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = ProtocolController.TAG, description = "Manage communication between PPRL services")
@RequestMapping(value = "protocol", produces = MediaType.APPLICATION_JSON_VALUE)
@Timed
public class ProtocolController {

  public static final String TAG = "PPRL protocol manager";

  private final ProtocolService protocolService;

  public ProtocolController(ProtocolService protocolService) {
    this.protocolService = protocolService;
  }

  @Operation(summary = "Transfer an encoded dataset", tags = TAG)
  @PostMapping("/transfer/dataset")
  public int transferEncoded(@RequestBody EncodedTransferRequestDto transferRequestDto) {
    return protocolService.addEncodedDataset(
      transferRequestDto.getDataOwnerDatasetId(),
      transferRequestDto.getEncoding().getMethod()
    );
  }

  @Operation(summary = "Create a new multilayer protocol definition", tags = TAG)
  @PostMapping("/multi-layer")
  public MultiLayerProtocol createMultiLayerProtocol(@RequestBody MultiLayerProtocol protocol) {
    return protocolService.createProtocol(protocol);
  }

  @Operation(summary = "Get a multilayer protocol definition", tags = TAG)
  @GetMapping("/multi-layer/{protocolId}")
  public MultiLayerProtocol getMultiLayerProtocol(@PathVariable String protocolId) {
    return protocolService.getProtocol(protocolId);
  }

  @Operation(summary = "Update a new multilayer protocol definition", tags = TAG)
  @PutMapping("/multi-layer")
  public MultiLayerProtocol updateMultiLayerProtocol(@RequestBody MultiLayerProtocol protocol) {
    return protocolService.updateProtocol(protocol);
  }

  @Operation(summary = "Delete a new multilayer protocol", tags = TAG)
  @DeleteMapping("/multi-layer/{protocolId}")
  public void deleteMultiLayerProtocol(@PathVariable String protocolId) {
    protocolService.deleteProtocol(protocolId);
  }

  @Operation(summary = "Delete all protocols", tags = TAG)
  @DeleteMapping("/multi-layer/all")
  public void deleteAll() {
    protocolService.deleteAllProtocols();
  }

  @Operation(summary = "Run a multilayer protocol", tags = TAG)
  @PostMapping("/multi-layer/run")
  public MultiLayerProtocol runMultiLayerProtocol(@RequestBody ProtocolExecutionDto protocolExecutionDto) {
    return protocolService.runProtocol(protocolExecutionDto);
  }

  @Operation(summary = "Skip next step of a multilayer protocol", tags = TAG)
  @PostMapping("/multi-layer/skip/{protocolId}")
  public MultiLayerProtocol skipStepOfMultiLayerProtocol(@PathVariable String protocolId) {
    return protocolService.skipNextStep(protocolId);
  }

  @Operation(summary = "Get all multilayer protocol definitions", tags = TAG)
  @GetMapping("/multi-layer/findAll")
  public List<MultiLayerProtocol> findAll() {
    return protocolService.getAllProtocols();
  }

  @Operation(summary = "Get an example multilayer protocol definition", tags = TAG)
  @GetMapping("/multi-layer/example/{protocolType}")
  public MultiLayerProtocol getExampleMultiLayerProtocol(@PathVariable(required = false) String protocolType) {
    return MultiLayerProtocolCreator.getExampleMultiLayerProtocol(protocolType, 2012);
  }

}

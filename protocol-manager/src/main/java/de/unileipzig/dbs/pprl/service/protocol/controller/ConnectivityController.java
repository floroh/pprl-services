package de.unileipzig.dbs.pprl.service.protocol.controller;

import de.unileipzig.dbs.pprl.service.protocol.service.DataOwnerService;
import de.unileipzig.dbs.pprl.service.protocol.service.LinkageUnitService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = ConnectivityController.TAG, description = "Test connectivity to other services.")
@RequestMapping(value = "connectivity", produces = MediaType.APPLICATION_JSON_VALUE)
@Timed
public class ConnectivityController {

  public static final String TAG = "Service connections";

  private final DataOwnerService dataOwnerService;
  private final LinkageUnitService linkageUnitService;

  public ConnectivityController(DataOwnerService dataOwnerService, LinkageUnitService linkageUnitService) {
    this.dataOwnerService = dataOwnerService;
    this.linkageUnitService = linkageUnitService;
  }

  @Operation(summary = "Test connections", tags = TAG)
  @GetMapping("/test")
  public boolean testConnections() {
    boolean doHealth = dataOwnerService.getEncoderApi().getHealth();
    boolean luHealth = linkageUnitService.getMatcherApi().getHealth();
    boolean dgHealth = dataOwnerService.getGeneratorApi().getHealth();
    return doHealth && luHealth && dgHealth;
  }

}

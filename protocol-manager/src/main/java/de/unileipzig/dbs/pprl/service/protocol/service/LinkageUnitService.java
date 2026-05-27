package de.unileipzig.dbs.pprl.service.protocol.service;

import de.unileipzig.dbs.pprl.service.common.services.DatasetIdService;
import de.unileipzig.dbs.pprl.service.protocol.api.MatcherApi;
import de.unileipzig.dbs.pprl.service.protocol.config.ServicesConfig;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for managing PPRL services
 */
@Service
@Slf4j
public class LinkageUnitService {

  private final ServicesConfig config;

  @Getter
  private final DatasetIdService datasetIdService;

  @Getter
  private final MatcherApi matcherApi = new MatcherApi();


  public LinkageUnitService(ServicesConfig config, DatasetIdService datasetIdService) {
    this.config = config;
    this.datasetIdService = datasetIdService;
  }

  @PostConstruct
  private void initApi() {
    log.info("Initialising LU Service with url: {}", config.getLinkageUnitEndpoint());
    matcherApi.setUrl(config.getLinkageUnitEndpoint());
  }

}

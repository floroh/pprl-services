package de.unileipzig.dbs.pprl.service.protocol.service;

import de.unileipzig.dbs.pprl.service.protocol.api.EncoderApi;
import de.unileipzig.dbs.pprl.service.protocol.api.MatcherApi;
import de.unileipzig.dbs.pprl.service.protocol.config.ServicesConfig;
import de.unileipzig.dbs.pprl.service.protocol.scripts.RecordInserter;
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
  private final MatcherApi matcherApi = new MatcherApi();

  @Getter
  private RecordInserter recordInserter = new RecordInserter();

  public LinkageUnitService(ServicesConfig config) {
    this.config = config;
  }

  @PostConstruct
  private void initApi() {
    log.info("Initialising LU Service with url: {}", config.getLinkageUnitEndpoint());
    MatcherApi.setUrl(config.getLinkageUnitEndpoint());
    recordInserter = new RecordInserter(config.getLinkageUnitEndpoint());
  }

}

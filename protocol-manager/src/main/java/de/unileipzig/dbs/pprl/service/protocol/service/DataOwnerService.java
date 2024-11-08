package de.unileipzig.dbs.pprl.service.protocol.service;

import de.unileipzig.dbs.pprl.service.protocol.api.EncoderApi;
import de.unileipzig.dbs.pprl.service.protocol.config.ServicesConfig;
import de.unileipzig.dbs.pprl.service.protocol.scripts.RecordInserter;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for managing Data Owner PPRL services
 */
@Service
@Slf4j
public class DataOwnerService {

  private final ServicesConfig config;

  @Getter
  private final EncoderApi encoderApi = new EncoderApi();

  @Getter
  private RecordInserter recordInserter = new RecordInserter();

  public DataOwnerService(ServicesConfig config) {
    this.config = config;
  }

  @PostConstruct
  private void initApi() {
    log.info("Initialising DO Service with url: {}", config.getDataOwnerEndpoint());
    EncoderApi.setUrl(config.getDataOwnerEndpoint());
    recordInserter = new RecordInserter(config.getDataOwnerEndpoint());
  }

}

package de.unileipzig.dbs.pprl.service.common.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DatasetIdService {

  public long generateDatasetId() {
    long datasetId = System.currentTimeMillis();
    log.info("Generated new dataset id: " + datasetId);
    return datasetId;
  }
}

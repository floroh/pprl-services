package de.unileipzig.dbs.pprl.service.protocol.service;

import de.unileipzig.dbs.pprl.core.matcher.classification.Classifier;
import de.unileipzig.dbs.pprl.core.matcher.evaluation.GroundTruth;
import de.unileipzig.dbs.pprl.service.common.data.converter.RecordConverter;
import de.unileipzig.dbs.pprl.service.common.data.converter.RecordIdPairConverter;
import de.unileipzig.dbs.pprl.service.common.data.dto.DatasetDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.EncodingIdDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordDto;
import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoGroundTruth;
import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoRecordIdPair;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.MatcherUpdateType;
import de.unileipzig.dbs.pprl.service.protocol.api.MatcherApi;
import de.unileipzig.dbs.pprl.service.protocol.config.ServicesConfig;
import de.unileipzig.dbs.pprl.service.protocol.model.dto.ProtocolExecutionDto;
import de.unileipzig.dbs.pprl.service.protocol.model.mongo.Layer;
import de.unileipzig.dbs.pprl.service.protocol.model.mongo.MultiLayerProtocol;
import de.unileipzig.dbs.pprl.service.protocol.persistence.repositories.MultiLayerProtocolRepository;
import de.unileipzig.dbs.pprl.service.protocol.scripts.RecordInserter;
import de.unileipzig.dbs.pprl.service.protocol.workflow.ProcessingStep;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing PPRL services
 */
@Service
@Slf4j
public class ProtocolService {

  private final ServicesConfig config;

  @Getter
  private final DataOwnerService dataOwnerService;

  @Getter
  private final LinkageUnitService linkageUnitService;

  private final MultiLayerProtocolRepository protocolRepository;

  public ProtocolService(ServicesConfig config, DataOwnerService dataOwnerService,
    LinkageUnitService linkageUnitService, MultiLayerProtocolRepository repository) {
    this.config = config;
    this.dataOwnerService = dataOwnerService;
    this.linkageUnitService = linkageUnitService;
    this.protocolRepository = repository;
  }

  public List<MultiLayerProtocol> getAllProtocols() {
    return protocolRepository.findAll();
  }

  public void deleteAllProtocols() {
    protocolRepository.findAll().forEach(p -> deleteProtocol(p.getProtocolId()));
  }

  public void deleteProtocol(String protocolId) {
    log.info("Deleting protocol " + protocolId);
    MultiLayerProtocol protocol = getProtocol(protocolId);
    getLinkageUnitService().getMatcherApi()
      .deleteProject(protocol.getLayers().getLast().getProjectId(), true);
    protocolRepository.deleteById(protocol.getObjId());
  }

  public MultiLayerProtocol updateProtocol(MultiLayerProtocol input) {
    log.info("Updating protocol " + input);
    MultiLayerProtocol protocol = getProtocol(input.getProtocolId());
    protocol.setStepHistory(input.getStepHistory());
    protocol.setStepQueue(input.getStepQueue());
    protocol.setLayers(input.getLayers());
    save(protocol);
    return protocol;
  }

  public MultiLayerProtocol skipNextStep(String protocolId) {
    MultiLayerProtocol protocol = getProtocol(protocolId);
    ProcessingStep next = protocol.getStepQueue().removeFirst();
    protocol.getStepHistory().add(next);
    save(protocol);
    return protocol;
  }

  public MultiLayerProtocol getProtocol(String protocolId) {
    ObjectId objId = new ObjectId(protocolId);
    Optional<MultiLayerProtocol> optionalProtocol = protocolRepository.findById(objId);
    if (optionalProtocol.isEmpty()) {
      throw new RuntimeException("Protocol with id " + protocolId + " not found");
    }
    return optionalProtocol.get();
  }

  public MultiLayerProtocol createProtocol(MultiLayerProtocol protocol) {
    MultiLayerProjectCreator runner = new MultiLayerProjectCreator(this);
    protocol = runner.initMultiLayerProtocol(protocol);
    save(protocol);
    protocol.setProtocolId(protocol.getObjId().toHexString());
    save(protocol);
    return protocol;
  }

  public void save(MultiLayerProtocol protocol) {
    protocol.updateLastUpdateToCurrentTime();
    protocolRepository.save(protocol);
  }

  public MultiLayerProtocol runProtocol(ProtocolExecutionDto protocolExecution) {
    ObjectId objId = new ObjectId(protocolExecution.getProtocolId());
    Optional<MultiLayerProtocol> optionalProtocol = protocolRepository.findById(objId);
    if (optionalProtocol.isEmpty()) {
      throw new RuntimeException("Could not find protocol with id " + protocolExecution.getProtocolId());
    }
    MultiLayerProtocol protocol = optionalProtocol.get();
//    MultiLayerProtocolFullRunner runner = new MultiLayerProtocolFullRunner(this);
    MultiLayerProtocolRunner runner = new MultiLayerProtocolRunner(this);
    protocol = runner.runMultiLayerProtocol(protocol, protocolExecution);
    save(protocol);
    return protocol;
  }

  public int addEncodedDataset(int dataOwnerDatasetId, String method) {
    if (dataOwnerDatasetId == 0) {
      throw new RuntimeException("Invalid dataset id '0'");
    }
    log.info("Encoding records of dataset {} ...", dataOwnerDatasetId);
    List<RecordDto> encRecords =
      dataOwnerService.getEncoderApi().retrieveEncoded(dataOwnerDatasetId, EncodingIdDto.builder()
        .method(method)
        .project("exampleProject")
        .build()
      );

    int encodedDatasetId = getEncodedDatasetId(dataOwnerDatasetId, method);
    log.info("Encoded dataset: " + encodedDatasetId);

    RecordConverter recordConverter = new RecordConverter();
    GroundTruth gtFromGlobalIds = GroundTruth.createFromGlobalIds(
      encRecords.stream().map(recordConverter::toRecord).collect(Collectors.toList()));
    log.info("Ground truth from global ids: " + gtFromGlobalIds.getIdPairs().size());
    MongoGroundTruth mongoGroundTruth = new MongoGroundTruth(
      encodedDatasetId,
      gtFromGlobalIds.getIdPairs().stream()
        .map(idP -> new MongoRecordIdPair(idP.getLeftRecordId(), idP.getRightRecordId(),
          Classifier.Label.TRUE_MATCH
        ))
        .collect(Collectors.toList())
    );

    int finalEncodedDatasetId = encodedDatasetId;
    encRecords.forEach(r -> {
      r.setDatasetId(finalEncodedDatasetId);
      r.getId().setBlocks(List.of(r.getId().getGlobal()));
      r.getId().setGlobal(null);
    });

    log.info("Deleting dataset " + encodedDatasetId + "...");
    linkageUnitService.getMatcherApi().deleteDataset(encodedDatasetId);
    log.info("Importing encoded records to matcher...");
    linkageUnitService.getRecordInserter().batchInsert(encRecords);
    linkageUnitService.getRecordInserter().addDatasetDescription(DatasetDto.builder()
      .plaintextDatasetId(dataOwnerDatasetId)
      .datasetId(finalEncodedDatasetId)
      .datasetName(method)
      .build());
    log.info("Importing ground truth to matcher...");
    new RecordInserter(MatcherApi.matcherUrl).addGroundTruth(RecordIdPairConverter.toDto(mongoGroundTruth));

    log.info("Finished");
    return encodedDatasetId;
  }

  public static int getEncodedDatasetId(int dataOwnerDatasetId, String method) {
    int encodedDatasetId = dataOwnerDatasetId;
    // ABF
    if (method.contains("ABF/")) {
      encodedDatasetId += 100;
    }
    if (method.contains("avg")) {
      encodedDatasetId += 200;
    }
    if (method.contains("xor")) {
      encodedDatasetId += 100;
    }
    return encodedDatasetId;
  }
}

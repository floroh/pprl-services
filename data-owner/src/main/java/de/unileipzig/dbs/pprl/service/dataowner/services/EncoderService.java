package de.unileipzig.dbs.pprl.service.dataowner.services;

import de.unileipzig.dbs.pprl.core.common.factories.AttributeFactory;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordId;
import de.unileipzig.dbs.pprl.core.encoder.KeyManager;
import de.unileipzig.dbs.pprl.core.encoder.crypto.KeyExtractor;
import de.unileipzig.dbs.pprl.core.encoder.record.RecordEncoder;
import de.unileipzig.dbs.pprl.service.common.data.converter.AbstractRecordConverter;
import de.unileipzig.dbs.pprl.service.common.data.converter.RecordConverter;
import de.unileipzig.dbs.pprl.service.common.data.dto.EncodingIdDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordIdDto;
import de.unileipzig.dbs.pprl.service.common.services.MetricsService;
import de.unileipzig.dbs.pprl.service.dataowner.data.dto.MultiRecordEncodingRequestDto;
import de.unileipzig.dbs.pprl.service.dataowner.data.dto.EncodingRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class EncoderService {

  public static final String LOCAL_ID_DUMMY = "DUMMY";

  private Map<EncodingIdDto, RecordEncoder> encoder = new HashMap<>();

  private final AbstractRecordConverter<RecordDto> dtoInConverter = new RecordConverter();

  private final AbstractRecordConverter<RecordDto> dtoOutConverter = new RecordConverter();

  private final EncoderProviderService encoderProviderService;

  private final SecretManagerService secretManagerService;

  public EncoderService(EncoderProviderService encoderProviderService,
    SecretManagerService secretManagerService) {
    this.encoderProviderService = encoderProviderService;
    this.secretManagerService = secretManagerService;
  }

  public RecordDto encode(EncodingRequestDto encodingRequest) {
    RecordDto dtoIn = encodingRequest.getRecord();
    RecordIdDto idIn = dtoIn.getId().duplicate();
    int datasetIdIn = dtoIn.getDatasetId();
    addDummyLocalIdIfMissing(dtoIn);

    Record in = dtoInConverter.toRecord(dtoIn);
    if (encodingRequest.getRecordSecret() != null) {
      in.setAttribute(
        KeyExtractor.KEY_ATTRIBUTE_NAME,
        AttributeFactory.getAttribute(encodingRequest.getRecordSecret())
      );
    }
    RecordDto out = encode(encodingRequest.getEncodingId(), in);
    out.setId(idIn);
    out.setDatasetId(datasetIdIn);
    return out;
  }

  public List<RecordDto> encode(MultiRecordEncodingRequestDto request) {
    int i = 0;
    long numRecords = request.getRecords().size();
    final List<RecordDto> encodedRecords = new ArrayList<>();
    for (RecordDto curRecord : request.getRecords()) {
      RecordDto encodedRecord = encode(
        EncodingRequestDto.builder()
          .encodingId(request.getEncodingId())
          .record(curRecord)
          .build()
      );
      encodedRecords.add(encodedRecord);
      i++;
      if (i % 10000 == 0) {
        log.info("Encoding record " + i + "/" + numRecords);
      }
    }
    return encodedRecords;
  }

  public RecordDto encode(EncodingIdDto encodingId, Record in) {
    Record out = innerEncode(encodingId, in);
    RecordDto outDto = dtoOutConverter.fromRecord(out);
    outDto.setEncodingId(encodingId);
    return outDto;
  }

  private Record innerEncode(EncodingIdDto encodingDto, Record in) {
    //TODO Optionally validate record and reject if crucial attributes are missing / unplausible
    MetricsService.counter("enc.counter").increment();
    setupKeyStore(encodingDto.getProject());
    initEncoderIfNotPresent(encodingDto);
    return encoder.get(encodingDto).encode(in);
  }

  private void setupKeyStore(String project) {
    Optional<KeyStore> optionalKeyStore = secretManagerService.getKeyStore(project);
    if (optionalKeyStore.isEmpty()) {
      throw new RuntimeException(
        "Abort encoding as no keystore is registered for project: " + project);
    }
    if (!KeyManager.getKeyStore().equals(optionalKeyStore.get())) {
      log.info("Activating keystore of project: " + project);
      KeyManager.setKeyStore(optionalKeyStore.get());
    }
  }

  private void initEncoderIfNotPresent(EncodingIdDto encodingIdDto) {
    if (!encoder.containsKey(encodingIdDto)) {
      log.info("Initialising encoder with id: " + encodingIdDto);
      RecordEncoder curEncoder = encoderProviderService.getEncoder(encodingIdDto);
      encoder.put(encodingIdDto, curEncoder);
    }
  }

  public void removeEncoder(EncodingIdDto encodingIdDto) {
    log.info("Removing encoder for id: " + encodingIdDto);
    int preCount = encoder.size();
    encoder.entrySet().removeIf(e -> e.getKey().isSubtypeOf(encodingIdDto));
    int diff = preCount - encoder.size();
    log.info("Removed " + diff + " encoder");
  }

  public void removeProject(String projectName) {
    log.info("Removing encoder for project: " + projectName);
    int preCount = encoder.size();
    encoder.entrySet().removeIf(e -> e.getKey().getProject().equals(projectName));
    int diff = preCount - encoder.size();
    log.info("Removed " + diff + " encoder");
  }

  /**
   * As a LocalId is mandatory for the internal {@link RecordId} but not for all use cases
   * a dummy id is used if it is missing.
   *
   * @param dtoIn plain record
   */
  private void addDummyLocalIdIfMissing(RecordDto dtoIn) {
    if (dtoIn.getId() == null) {
      dtoIn.setId(RecordIdDto.builder().local(LOCAL_ID_DUMMY).build());
    } else if (dtoIn.getId().getLocal() == null) {
      RecordIdDto idDto = dtoIn.getId();
      idDto.setLocal("DUMMY");
      dtoIn.setId(idDto);
    }
  }
}

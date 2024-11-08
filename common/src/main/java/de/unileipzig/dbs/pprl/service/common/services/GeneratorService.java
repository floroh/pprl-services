package de.unileipzig.dbs.pprl.service.common.services;

import de.unileipzig.dbs.pprl.core.common.factories.AttributeFactory;
import de.unileipzig.dbs.pprl.core.common.factories.RecordFactory;
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordId;
import de.unileipzig.dbs.pprl.core.common.model.impl.RecordIdMap;
import de.unileipzig.dbs.pprl.core.common.serialization.AttributeSerializationType;
import de.unileipzig.dbs.pprl.core.common.model.impl.PersonalAttributeType;
import de.unileipzig.dbs.pprl.core.matcher.classification.Classifier;
import de.unileipzig.dbs.pprl.service.common.data.converter.RecordConverter;
import de.unileipzig.dbs.pprl.service.common.data.dto.AttributeDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.GroundTruthDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordIdDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordIdPairDto;
import de.unileipzig.dbs.pprl.core.matcher.evaluation.GroundTruth;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GeneratorService {

  private int datasetId = -1;

  private final List<ExamplePlainRecord> plainRecords = List.of(
    new ExamplePlainRecord("dup", "0", "0g",
      "Ruth", "Bauer", "03.08.1971", "61348", "Graevenwiesbach"
    ),
    new ExamplePlainRecord("org", "0", "0g",
      "Ruth", "Bauer", "02.08.1971", "61348", "Graevenwiesbach"
    ),
    new ExamplePlainRecord("dup", "1", "1g",
      "Carsten", "Noak", "23.05.1962", "50997", "Koeln"
    ),
    new ExamplePlainRecord("org", "1", "1g",
      "Carsten", "Nowak", "23.05.1962", "50997", "Koeln"
    ),
    new ExamplePlainRecord("dup", "2", "2g",
      "Karl", "Kampe", "19.06.1947", "74842", "Billigheim"
    ),
    new ExamplePlainRecord("org", "2", "2g",
      "Karl-Heinrich", "Kampe", "19.06.1947", "74842", "Billigheim"
    ),
    new ExamplePlainRecord("dup", "3", "3g",
      "Beate", "Gross", "03.02.1984", "56727", "Reudelsterz"
    ),
    new ExamplePlainRecord("org", "3", "3g",
      "Beate", "Schuster", "03.02.1984", "56727", "Reudelsterz"
    ),
    new ExamplePlainRecord("dup", "4", "4g",
      "Svetlana", "Brueckner", "17.06.1938", "86916", "Kaufering"
    ),
    new ExamplePlainRecord("org", "4", "4g",
      "Svetlana", "Brueckner", "17.06.1938", "22941", "Jersbek"
    ),
    new ExamplePlainRecord("org", "10", "10g",
      "Beate", "Meier", "02.11.1981", "13125", "Berlin"
    ),
    new ExamplePlainRecord("org", "11", "11g",
      "Margarete", "Gehrke", "15.04.1953", "10407", "Berlin"
    ),
    new ExamplePlainRecord("org", "12", "12g",
      "Ruben", "Kampe", "23.01.1947", "71686", "Asperg"
    ),
    new ExamplePlainRecord("dup", "20", "20g",
      "Gisela", "Schneider", "22.07.1944", "46499", "Hamminkeln"
    ),
    new ExamplePlainRecord("dup", "21", "21g",
      "Anna", "Lind", "27.07.1979", "24214", "Tuettendorf"
    ),
    new ExamplePlainRecord("dup", "22", "22g",
      "Gottfried", "Schumacher", "09.04.1953", "10117", "Berlin"
    )
  );

  private final List<ExampleRbfRecord> rbfRecords = List.of(
    new ExampleRbfRecord("dup", "0", "0g",
      "EME8OjSwU5AIFQhAEAhBBYSGQkBEhiSBlDYRCRgYgFQGCCQtAgJ0SEGKJAgEAgFOggeQKIJEACGUD5kkEMUgsgKIFxghjgg4CsgWIAL4O2AASUhGdJK0DESBCgD6cYp4dncmYAEYBfCVOg0BRtHxAISMAB+AyUiACCYxIIeA8GQ="
    ),
    new ExampleRbfRecord("org", "0", "0g",
      "UME8OjWwU5EMFQpAEAhBBYSGQkFEBiSBlDaBCTgYhFQESGQtAAB0SkGLJAgUAgFOgg6QKIJEADGUDZkkEMUgIAKIN1ghDAk4CphWIgLYu0AASQgGcLK0DESBCgJ6cYpwdlcmYAUYBHCVOg0BREnxAISMAB8AjQiASS4RIAeA9GQ="
    ),
    new ExampleRbfRecord("dup", "1", "1g",
      "ECCTZGSqAWB7AQKEZFARhiQRqhgAEGBmxCsySiUQSAGChgAmCovrhSkhfVAoAMneTDEDjJKgYEqgCiY0E3AStKdNbikjAgAGK8LTMldgA2QCutrEEkECgyKdAFBQDAAAQBvAEgEIDaoAXBDZg5rD1AuoMwyQxpzXReAJDIAxAow="
    ),
    new ExampleRbfRecord("org", "1", "1g",
      "ECASZGSqAWA7AQKEZFIRhiRVqFgEEGBmxCsySiUQSAGCxgAmCovrhSkhfxAIAMjfDDEDjpKgQEqgCiYyE3AStKXMZ6krIgAGK8LTNlNgA2wCutrEElEAgyKdAFBQDAIAQJvAEgEIDaoAXDDZg5rDVguoMwzQxpz3ReEJDIA1Aow="
    ),
    new ExampleRbfRecord("dup", "2", "2g",
      "ICnkoAag0E4AklBMVdiAgHIaCTkYm0OY1AIasqAHFwoIBAsqYsiRACgAYcBgYHYW2gLnmUCkJ4gLgAwoAANE4kMsi8SErPRALYKEAQRgJCSAk6GnkEiAvJCoALAQCAEGPgURQAIAYCAJGIk7AAAMhFuIsBUTVCkWlpCYIdLEqBI="
    ),
    new ExampleRbfRecord("org", "2", "2g",
      "oGnlohbg0c4eklFcVdiClOIaCTkZm8eZVEYbs6IPkwoARCoqesnxAioIachmYn9W2wPjmcL0J6grgYwoQBNG4kNsq8SVrPRQL4KGBQToNjSCsem3kP3EvJKpVLQaCgECPxcxUxIAYGQJWom7ESsNhHuYshVT1DUWtpDYoYvMqhI="
    ),
    new ExampleRbfRecord("dup", "3", "3g",
      "RkgJFI0okDoCRUozkGUMRAYGZ02JhAYKweARpCV5MCjeA6gvSqNzCUekIAJMLMuBsDGAAYABBGlgkoACQNBGuhqKjHJYogWgWGGiIMOiACCAhF3YBcAB1UDTAwvYFJB5ICBAjSBhCYCgAoNAA/bkApsqwh6jQUvIFEAgAIAQAmA="
    ),
    new ExampleRbfRecord("org", "3", "3g",
      "DAoBFJRowTgqEVnnAHAuRBQUon7LhAgGxbARBiW7cDpWWCguCotzFBKkICJMLguLsD2ZEYZBgYkAloiiRNMG9hKogvJ4pwWEWOGCMMeygCIAjFnaBdDBzUDRgwjYFtJ9FCEALwFpCZGxgqMVS5bsQpsuwB6jQUuIHEA4gII4BiI="
    ),
    new ExampleRbfRecord("dup", "4", "4g",
      "E6QmIIrEcOYZsE3bCA6NuDpnIltt4FbYBFCPtBU0GvEh38SAJpF0UK8IQMVtTgQKGQZlHQJGAkAKPPkoADNZksANupw0hEzGjsXOYRG0AUyQKvSiCADpaPLyKgtwpEjG1DEI0QenUEm5NA4h4mVURtEORJEW2O2UDq0hL1qQkmI="
    ),
    new ExampleRbfRecord("org", "4", "4g",
      "E6QmIIrEcOYZsE3bCA6NuDpnIltt4FbYBFCPtBU0GvEh38SAJpF0UK8IQMVtTgQKGQZlHQJGAkAKPPkoADNZksANupw0hEzGjsXOYRG0AUyQKvSiCADpaPLyKgtwpEjG1DEI0QenUEm5NA4h4mVURtEORJEW2O2UDq0hL1qQkmI="
    ),
    new ExampleRbfRecord("org", "10", "10g",
      "SIkBFDsY1VWGA0JjEOAAFQynYk1pAAlD5aKIFKEwJCpAyGEmgCB2gwLhICCcAgqJsGzDAIJDCBsEBIkhQNAMcBI4oHJYJQWBRLFCItDQgiAAShiIYWLCjRCRgVPMEAB5VANIDUVtAADZgqYJQRjmA9Mu4B6gxQqAVW0AEIAwxg0="
    ),
    new ExampleRbfRecord("org", "11", "11g",
      "xzSNJA7kAMFB5SbO0UHwg0couDqddkxIoLIS4qIJcf6uJEYhBt0Xi1yrCABqTJgiMEREAASCYSiGwBCYUF" +
        "+Qq1COgOKGoX3QRaBEAWJAMUWCEKLPRufQmp7mNUJ+QgAKCMAth40hEmEPECjEAVgQg8f1UEjCNcgWBcgMEBSEioI="
    ),
    new ExampleRbfRecord("org", "12", "12g",
      "IEnAJgCikdggn1gOFXgRAsIAi4UYm0HAdAAzWoQlAQtAgiorYsiRACoApcgmMPKXsgGRnEpgBQBZiwwoQBUUkockOkC2jDAgKGoHIlJwJSSCuWOTwMiEGJCMACAS6AASHAXRRAMIYYqABAUIgJCwwJkIkhWRyCBTkGCQiZL06AI="
    ),
    new ExampleRbfRecord("dup", "20", "20g",
      "dqMiEJT8SW3vmB/ogQIeSRwdemX0diAKFEGpBrRYMCiJWtiglopTRAQFIuTlDiAKEFz5gOJwkXqIbKigBBMyMwGgUvR" +
        "/LSUAiZMHM7aUAAKAy/PUsADtSgwot+tAiKpunIE1NkOHAgDxpacdYAjYYY8EAgCgBVyEWx2RHIsQAAE="
    ),
    new ExampleRbfRecord("dup", "21", "21g",
      "RQIoYByIBnwDAAVwImZsobhrEBwwAnAcNNSJtRBkupiJB4SEzahdSiEAQMRpQABICcIBXFojUgAAKCEkSFEfAwIgAPSqoUAAGAKBAnLEAAABcOAkFgQkCmOCgGMgtTQgQg0CAQHZAkELJgoYqnaoUAqqQpkAgdAQEKyhGQwmQAc="
    ),
    new ExampleRbfRecord("dup", "22", "22g",
      "B66QMHVuWRWgHhfogZK6ZGwcenLHBERQptA" +
        "/WxaQQ1bUXMhzMhpVzXQLoSFmTqqqoUaaEcdz9Y0G1ri4wdsE2hOKi9e3ZMH8FJSMITS4SAZUi5I7ANfHCA4kodBhGYOXHlMKtwlDMoOVo6I1UCXoZO41EgszY4+GHdD1nDMARJM="
    )
  );

  public int getDatasetId() {
    return datasetId;
  }

  public void setDatasetId(int datasetId) {
    this.datasetId = datasetId;
  }

  public List<RecordDto> getPlainRecordDtos() {
    return plainRecords.stream()
      .map(ExamplePlainRecord::getAsRecordDto)
      .peek(dto -> dto.setDatasetId(datasetId))
      .collect(Collectors.toList());
  }

  public List<RecordDto> getRbfRecordDtos() {
    return rbfRecords.stream()
      .map(ExampleRbfRecord::getAsRecordDto)
      .peek(dto -> dto.setDatasetId(datasetId))
      .collect(Collectors.toList());
  }

  public GroundTruthDto getGroundTruthDto() {
    GroundTruth gt =
      GroundTruth.createFromGlobalIds(plainRecords.stream().map(ExamplePlainRecord::getAsRecord).collect(
        Collectors.toList()));
    List<RecordIdPairDto> idPairDtos = gt.getIdPairs().stream()
      .map(pair -> RecordIdPairDto.builder()
        .leftRecordId(RecordConverter.fromRecordId(pair.getLeftRecordId()))
        .rightRecordId(RecordConverter.fromRecordId(pair.getRightRecordId()))
        .label(Classifier.Label.TRUE_MATCH)
        .build())
      .collect(Collectors.toList());
    return GroundTruthDto.builder()
      .datasetId(-1)
      .recordIdPairs(idPairDtos)
      .build();
  }

  public RecordDto getPlainRecord(boolean withId) {
    RecordDto dto = RecordDto.builder()
      .attribute(PersonalAttributeType.FIRSTNAME.name(), getStringAttribute("Peter"))
      .attribute(PersonalAttributeType.LASTNAME.name(), getStringAttribute("Müller"))
      .attribute(PersonalAttributeType.DATEOFBIRTH.name(), getStringAttribute("06.05.1980"))
      .attribute(PersonalAttributeType.GENDER.name(), getStringAttribute("m"))
      .attribute(PersonalAttributeType.ADDRESS.name(), getStringAttribute("04105 Leipzig"))
      .build();
    if (withId) {
      dto.setId(
        RecordIdDto.builder()
          .local("ID0")
          .source("KDLA")
          .build()
      );
    }
    return dto;
  }

  private AttributeDto getStringAttribute(String value) {
    return AttributeDto.builder()
      .type(AttributeSerializationType.STRING.name())
      .value(value)
      .build();
  }

  @Data
  @AllArgsConstructor
  private class ExampleRbfRecord {
    private String source;
    private String local;
    private String global;

    private String rbf;

    public Record getAsRecord() {
      Record record = RecordFactory.getEmptyRecord(new RecordIdMap(local, source));
      record.getId().addId(RecordId.GLOBAL_ID, global);
      record.setAttribute("RBF", asBvAttr(rbf));
      return record;
    }

    public RecordDto getAsRecordDto() {
      return RecordDto.builder()
        .id(RecordIdDto.builder()
          .local(local)
          .source(source)
          .global(global)
          .build())
        .attribute("RBF", AttributeDto.builder()
          .type(AttributeSerializationType.BITSET_BASE64.name())
          .value(rbf)
          .build())
        .build();
    }

    private Attribute asBvAttr(String value) {
      return AttributeFactory.parseAttribute(AttributeSerializationType.BITSET_BASE64, value);
    }
  }

  @Data
  @AllArgsConstructor
  private class ExamplePlainRecord {
    private String source;
    private String local;
    private String global;

    private String firstname;
    private String lastname;
    private String dateofbirth;
    private String zip;
    private String city;

    public Record getAsRecord() {
      Record record = RecordFactory.getEmptyRecord(new RecordIdMap(local, source));
      record.getId().addId(RecordId.GLOBAL_ID, global);
      record.setAttribute(PersonalAttributeType.FIRSTNAME.name(), asStringAttr(firstname));
      record.setAttribute(PersonalAttributeType.LASTNAME.name(), asStringAttr(lastname));
      record.setAttribute(PersonalAttributeType.DATEOFBIRTH.name(), asStringAttr(dateofbirth));
      record.setAttribute(PersonalAttributeType.PLZ.name(), asStringAttr(zip));
      record.setAttribute(PersonalAttributeType.CITY.name(), asStringAttr(city));
      return record;
    }

    public RecordDto getAsRecordDto() {
      return RecordDto.builder()
        .id(RecordIdDto.builder()
          .local(local)
          .source(source)
          .global(global)
          .build())
        .attribute(PersonalAttributeType.FIRSTNAME.name(), AttributeDto.builder()
          .type(AttributeSerializationType.STRING.name())
          .value(firstname)
          .build())
        .attribute(PersonalAttributeType.LASTNAME.name(), AttributeDto.builder()
          .type(AttributeSerializationType.STRING.name())
          .value(lastname)
          .build())
        .attribute(PersonalAttributeType.DATEOFBIRTH.name(), AttributeDto.builder()
          .type(AttributeSerializationType.STRING.name())
          .value(dateofbirth)
          .build())
        .attribute(PersonalAttributeType.PLZ.name(), AttributeDto.builder()
          .type(AttributeSerializationType.STRING.name())
          .value(zip)
          .build())
        .attribute(PersonalAttributeType.CITY.name(), AttributeDto.builder()
          .type(AttributeSerializationType.STRING.name())
          .value(city)
          .build())
        .build();
    }

    private Attribute asStringAttr(String value) {
      return AttributeFactory.parseAttribute(AttributeSerializationType.STRING, value);
    }

  }
}

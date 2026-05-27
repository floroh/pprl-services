package de.unileipzig.dbs.pprl.service.protocol.api;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.unileipzig.dbs.pprl.service.generator.data.dto.GermanyGeneratorConfig;
import de.unileipzig.dbs.pprl.service.generator.data.dto.TaggedDatasetDto;
import de.unileipzig.dbs.pprl.service.generator.data.dto.UsvrSelectionConfig;
import de.unileipzig.dbs.pprl.service.protocol.csv.JacksonObjectMapper;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Slf4j
public class GeneratorApi {

  public String url = "http://localhost:8086";

  public GeneratorApi(String url) {
    this();
    this.url = url;
  }

  public GeneratorApi() {
    Unirest.config().reset();
    Unirest.config()
      .addDefaultHeader("Content-Type", "application/json")
      .socketTimeout(3_600_000)  // 60min
      .setObjectMapper(new JacksonObjectMapper());
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public boolean getHealth() {
    String endPoint = url + "/actuator/health";
    try {
      HttpResponse<String> response = Unirest.get(endPoint)
              .asString();
      log.info("Health of " + url + ": " + response.getBody());
      return response.isSuccess();
    } catch (Exception e) {
      log.error("Health of " + url + ": failed with " + e.getMessage());
      return false;
    }
  }

  public TaggedDatasetDto generateGermanRecords(GermanyGeneratorConfig configuration) {
    String endPoint = url + "/generator/generate";
    return getLargeTaggedDataset(endPoint, configuration);
  }

  public TaggedDatasetDto selectUsvrRecords(UsvrSelectionConfig configuration) {
    String endPoint = url + "/selector/select";
    return getLargeTaggedDataset(endPoint, configuration);

  }

  /**
   * Workaround to enable fetching large datasets (with tags), e.g. 1M from Germany Generator
   * otherwise unirest has a OutOfMemoryException because the response does not fit in a utf16 string
   */
  private TaggedDatasetDto getLargeTaggedDataset(String endPoint, Object body) {
    HttpResponse<byte[]> response = Unirest.post(endPoint)
            .body(body)
            .asBytes();
    log.debug("Got response. Parsing it.");
    try (ByteArrayInputStream is = new ByteArrayInputStream(response.getBody())) {
      ObjectMapper mapper = new ObjectMapper();
      return mapper.readValue(is, TaggedDatasetDto.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private TaggedDatasetDto getTaggedDataset(String endPoint, Object body) {
    return Unirest.post(endPoint)
            .body(body)
            .asObject(TaggedDatasetDto.class)
            .getBody();
  }

}

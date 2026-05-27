package de.unileipzig.dbs.pprl.service.common.data.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static de.unileipzig.dbs.pprl.service.common.Constants.DUMMY_LINKAGE_PROJECT;

class EncodingIdDtoTest {

  @Test
  void serializeEncoding() throws JsonProcessingException {
    EncodingIdDto dto = EncodingIdDto.builder()
      .project(DUMMY_LINKAGE_PROJECT)
      .method("DBSLeipzig/RBF")
      .build();
    ObjectMapper om = new ObjectMapper();
    String jsonString = om.writerWithDefaultPrettyPrinter().writeValueAsString(dto);
//    System.out.println(jsonString);
  }
}
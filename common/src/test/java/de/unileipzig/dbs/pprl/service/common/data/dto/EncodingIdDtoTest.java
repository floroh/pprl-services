package de.unileipzig.dbs.pprl.service.common.data.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class EncodingIdDtoTest {

  @Test
  void serializeEncoding() throws JsonProcessingException {
    EncodingIdDto dto = EncodingIdDto.builder()
      .project("exampleProject")
      .method("DBSLeipzig/RBF")
      .build();
    ObjectMapper om = new ObjectMapper();
    String jsonString = om.writerWithDefaultPrettyPrinter().writeValueAsString(dto);
//    System.out.println(jsonString);
  }
}
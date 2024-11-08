package de.unileipzig.dbs.pprl.service.common.data.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RecordIdDtoTest {

  @Test
  void serializeNonGlobalId() throws JsonProcessingException {
    RecordIdDto id = RecordIdDto.builder()
      .local("ID0")
      .source("org")
      .build();
    ObjectMapper om = new ObjectMapper();
    String jsonString = om.writerWithDefaultPrettyPrinter().writeValueAsString(id);
    assertFalse(jsonString.contains("global"));
  }

}
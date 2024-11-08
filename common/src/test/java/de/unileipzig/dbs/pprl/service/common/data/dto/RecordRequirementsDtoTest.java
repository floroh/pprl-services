package de.unileipzig.dbs.pprl.service.common.data.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class RecordRequirementsDtoTest {

  @Test
  void serialize() throws JsonProcessingException {
    RecordRequirementsDto dto = RecordRequirementsDto.builder()
      .method("DBSLeipzig/RBF")
//      .requiredAttribute(PersonalAttributeType.FIRSTNAME.name())
//      .requiredAttribute(PersonalAttributeType.LASTNAME.name())
      .build();
    ObjectMapper om = new ObjectMapper();
    String jsonString = om.writerWithDefaultPrettyPrinter().writeValueAsString(dto);
//    System.out.println(jsonString);
  }
}
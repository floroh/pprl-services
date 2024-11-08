package de.unileipzig.dbs.pprl.service.common.data.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.unileipzig.dbs.pprl.core.common.model.impl.BitSetVector;
import de.unileipzig.dbs.pprl.core.common.model.api.BitVector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@JsonTest
class BitVectorRecordDtoTest {

  private JacksonTester<BitVectorRecordDto> json;

  private BitVector bv;

  @BeforeEach
  void setup() {
    ObjectMapper objectMapper = new ObjectMapper();
    JacksonTester.initFields(this, objectMapper);

    bv = new BitSetVector(64);
    bv.set(2);
    bv.set(8);
    bv.set(20);
    bv.set(61);
  }

  @Test
  void serialize() throws IOException {
    BitVectorRecordDto dto = BitVectorRecordDto.builder()
      .id(RecordIdDto.builder().local("ID0").build())
      .attribute("rbf", bv.getBase64())
      .build();

    String jsonString = json.write(dto).getJson();
    assertNotNull(jsonString);
    assertFalse(jsonString.isEmpty());
//    System.out.println(jsonString);
  }

  @Test
  void deserialize() throws IOException {
    String jsonString = """
      {
          "id.local": "ID0",
          "id.source": "KDLA",
          "attributes": {
              "rbf": "BAEQAAAAACA="
          }
      }""";

    BitVectorRecordDto dto = json.parse(jsonString).getObject();
    assertNotNull(dto);
    assertEquals("ID0", dto.getId().getLocal());
    String rbf = dto.getAttributes().get("rbf");
    assertNotNull(rbf);
    BitVector bvClone = BitSetVector.fromBase64(rbf);
    assertEquals(bv, bvClone);
  }
}
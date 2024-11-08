package de.unileipzig.dbs.pprl.service.common.data.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.unileipzig.dbs.pprl.core.common.serialization.AttributeSerializationType;
import de.unileipzig.dbs.pprl.core.common.model.api.BitVector;
import de.unileipzig.dbs.pprl.core.common.model.impl.BitSetVector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@JsonTest
class RecordDtoTest {

  private JacksonTester<RecordDto> json;

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
    RecordDto dto = RecordDto.builder()
      .id(RecordIdDto.builder().local("ID0").build())
      .attribute("bf", AttributeDto.builder()
        .type(AttributeSerializationType.BITSET_BASE64.name())
        .value(bv.getBase64()).build())
      .attribute("housenumber", AttributeDto.builder()
        .type(AttributeSerializationType.INT.name())
        .value("1233").build())
      .attribute("name", AttributeDto.builder()
        .type(AttributeSerializationType.STRING.name())
        .value("Peter").build())
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
        "id": {
          "local": "ID0"
        },
        "attributes": {
          "bf": {
            "type": "BITSET_BASE64",
            "value": "BAEQAAAAACA="
          },
          "housenumber": {
            "type": "INT",
            "value": "1233"
          },
          "name": {
            "type": "STRING",
            "value": "Peter"
          }
        }
      }""";

    RecordDto dto = json.parse(jsonString).getObject();
    assertNotNull(dto);
    assertEquals("ID0", dto.getId().getLocal());
    String str = dto.getAttributes().get("bf").getValue();
    assertNotNull(str);
    BitVector bvClone = BitSetVector.fromBase64(str);
    assertEquals(bv, bvClone);

    str = dto.getAttributes().get("housenumber").getValue();
    assertNotNull(str);
    assertEquals(1233, Integer.parseInt(str));

    str = dto.getAttributes().get("name").getValue();
    assertNotNull(str);
    assertEquals("Peter", str);

  }
}
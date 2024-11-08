package de.unileipzig.dbs.pprl.core.encoder.record;

import de.unileipzig.dbs.pprl.core.common.factories.AttributeFactory;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.impl.PersonalAttributeType;
import de.unileipzig.dbs.pprl.core.encoder.RecordEncoderSerialization;
import de.unileipzig.dbs.pprl.core.encoder.TestBase;
import de.unileipzig.dbs.pprl.core.encoder.crypto.KeyExtractor;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SelectivePlainProviderTest extends TestBase {

  @Test
  void withAvailableAttributes() {
    SelectivePlainProvider encoder = new SelectivePlainProvider();

    Record record = getPersonalRecord();
    record.setAttribute(
      KeyExtractor.KEY_ATTRIBUTE_NAME,
      AttributeFactory.getAttribute(String.join(SelectivePlainProvider.ATTRIBUTENAME_SEPARATOR,
        PersonalAttributeType.FIRSTNAME.asString(),
        PersonalAttributeType.DATEOFBIRTH.asString()
      ))
    );

    Record output = encoder.encode(record);
    assertTrue(output.getAttribute(PersonalAttributeType.FIRSTNAME.asString()).isPresent());
    assertFalse(output.getAttribute(PersonalAttributeType.LASTNAME.asString()).isPresent());
    assertTrue(output.getAttribute(PersonalAttributeType.DATEOFBIRTH.asString()).isPresent());
  }


  @Test
  void withGlobalAttributeNames() {
    SelectivePlainProvider encoder = new SelectivePlainProvider();
    encoder.setGlobalAttributeNames(Set.of(PersonalAttributeType.FIRSTNAME.asString(),
      PersonalAttributeType.DATEOFBIRTH.asString()));
    String jsonString = RecordEncoderSerialization.serializeJson(encoder);
    System.out.println(jsonString);
    Record recordWithSpecificAttributes = getPersonalRecord(0);
    recordWithSpecificAttributes.setAttribute(
      KeyExtractor.KEY_ATTRIBUTE_NAME,
      AttributeFactory.getAttribute(String.join(SelectivePlainProvider.ATTRIBUTENAME_SEPARATOR,
        PersonalAttributeType.FIRSTNAME.asString(),
        PersonalAttributeType.LASTNAME.asString()
      ))
    );

    Record output = encoder.encode(recordWithSpecificAttributes);
    assertTrue(output.getAttribute(PersonalAttributeType.FIRSTNAME.asString()).isPresent());
    assertTrue(output.getAttribute(PersonalAttributeType.LASTNAME.asString()).isPresent());
    assertFalse(output.getAttribute(PersonalAttributeType.DATEOFBIRTH.asString()).isPresent());

    Record record = getPersonalRecord(1);
    output = encoder.encode(record);
    assertTrue(output.getAttribute(PersonalAttributeType.FIRSTNAME.asString()).isPresent());
    assertFalse(output.getAttribute(PersonalAttributeType.LASTNAME.asString()).isPresent());
    assertTrue(output.getAttribute(PersonalAttributeType.DATEOFBIRTH.asString()).isPresent());
  }

  @Test
  void serialize() {
    SelectivePlainProvider encoder = new SelectivePlainProvider();
    String jsonString = RecordEncoderSerialization.serializeJson(encoder);
    assertFalse(jsonString.isEmpty());
  }
}
package de.unileipzig.dbs.pprl.core.common;

import de.unileipzig.dbs.pprl.core.common.factories.RecordFactory;
import de.unileipzig.dbs.pprl.core.common.factories.RecordIdFactory;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.impl.AttributeLight;
import de.unileipzig.dbs.pprl.core.common.model.impl.ListAttributeLight;
import de.unileipzig.dbs.pprl.core.common.model.impl.PersonalAttributeType;

import java.util.Arrays;

public class CommonTestBase {

  protected Record getPersonalRecord() {
    return getPersonalRecord(0);
  }

  protected Record getPersonalRecord(int i) {
    Record record = RecordFactory.getEmptyRecord(RecordIdFactory.get("record" + i));
    switch (i) {
      case 0:
        record.setAttribute(PersonalAttributeType.FIRSTNAME.toString(), new AttributeLight("Peter"))
          .setAttribute(PersonalAttributeType.LASTNAME.toString(), new AttributeLight("Pan"))
          .setAttribute(PersonalAttributeType.DATEOFBIRTH.toString(), new AttributeLight("29.01.2019"));
        break;
      case 1:
        record.setAttribute(PersonalAttributeType.FIRSTNAME.toString(), new AttributeLight("Petra"))
          .setAttribute(PersonalAttributeType.LASTNAME.toString(), new AttributeLight("Pan"))
          .setAttribute(PersonalAttributeType.DATEOFBIRTH.toString(), new AttributeLight("29.10.2019"));
        break;
      case 2:
        record.setAttribute(
            PersonalAttributeType.FIRSTNAME.toString(),
            new ListAttributeLight(Arrays.asList("Petra", "Johanna"))
          )
          .setAttribute(
            PersonalAttributeType.LASTNAME.toString(),
            new ListAttributeLight(Arrays.asList("Pan", "Hook"))
          )
          .setAttribute(PersonalAttributeType.DATEOFBIRTH.toString(), new AttributeLight("29.10.2019"));
        break;
      case 3:
        record.setAttribute(PersonalAttributeType.FIRSTNAME.toString(), new AttributeLight("Petra Johanna"))
          .setAttribute(PersonalAttributeType.LASTNAME.toString(), new AttributeLight("Pan-Hook"))
          .setAttribute(PersonalAttributeType.DATEOFBIRTH.toString(), new AttributeLight("29.10.2019"));
        break;
    }
    return record;
  }
}

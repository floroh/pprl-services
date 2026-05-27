package de.unileipzig.dbs.pprl.service.dataowner.generator;

import de.unileipzig.dbs.pprl.core.common.HashUtils;
import de.unileipzig.dbs.pprl.core.common.factories.AttributeFactory;
import de.unileipzig.dbs.pprl.core.common.factories.RecordFactory;
import de.unileipzig.dbs.pprl.core.common.factories.RecordIdFactory;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;

import java.util.List;
import java.util.Random;

public class RecordGenerator {

  public static final int ATTRIBUTE_LENGTH = 8;

  private final List<String> attributeNames;

  public RecordGenerator(List<String> attributeNames) {
    this.attributeNames = attributeNames;
  }

  public Record getRecord(int seed) {
    Record record = RecordFactory.getEmptyRecord(
      RecordIdFactory.get(Integer.toString(seed))
    );
    for (String attributeName : attributeNames) {
      record.setAttribute(attributeName,
        AttributeFactory.getAttribute(getRandomValue(seed + attributeName)));
    }
    return record;
  }

  private String getRandomValue(String seed) {
    int leftLimit = 97; // letter 'a'
    int rightLimit = 122; // letter 'z'
    Random random = HashUtils.getRandom(seed, seed);

    String generatedString = random.ints(leftLimit, rightLimit + 1)
      .limit(ATTRIBUTE_LENGTH)
      .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
      .toString();
    return generatedString;
  }
}

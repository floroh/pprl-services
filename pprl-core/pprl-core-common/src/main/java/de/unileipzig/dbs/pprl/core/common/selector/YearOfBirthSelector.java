package de.unileipzig.dbs.pprl.core.common.selector;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.impl.PersonalAttributeType;
import org.apache.commons.lang3.ArrayUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.stream.IntStream;

public class YearOfBirthSelector implements Selector<Record> {

  public static final String DEFAULT_ATTRIBUTE_NAME = PersonalAttributeType.DATEOFBIRTH.asString();

  private String attributeName;

  private String inputDatePattern;

  private int[] values;

  @JsonIgnore
  private DateTimeFormatter dateFormatter;

  public YearOfBirthSelector(String inputDatePattern, int startYearInclusive, int endYearExclusive) {
    this(DEFAULT_ATTRIBUTE_NAME, inputDatePattern, startYearInclusive, endYearExclusive);
  }

  public YearOfBirthSelector(String attributeName, String inputDatePattern,
    int startYearInclusive, int endYearExclusive) {
    this(attributeName, inputDatePattern, IntStream.range(startYearInclusive, endYearExclusive).toArray());
  }

  public YearOfBirthSelector(String attributeName, String inputDatePattern, int[] values) {
    this.attributeName = attributeName;
    this.inputDatePattern = inputDatePattern;
    this.values = values;
  }

  @Override
  public boolean test(Record record) {
    if (dateFormatter == null) {
      this.dateFormatter = DateTimeFormatter.ofPattern(inputDatePattern);
    }
    Optional<Attribute> optionalAttribute = record.getAttribute(attributeName);
    if (optionalAttribute.isEmpty()) {
      return false;
    }
    String asString = optionalAttribute.get().getAsString();
    LocalDate date = LocalDate.parse(asString, dateFormatter);
    return ArrayUtils.contains(values, date.getYear());
  }

  public String getAttributeName() {
    return attributeName;
  }

  public String getInputDatePattern() {
    return inputDatePattern;
  }

  public int[] getValues() {
    return values;
  }
}

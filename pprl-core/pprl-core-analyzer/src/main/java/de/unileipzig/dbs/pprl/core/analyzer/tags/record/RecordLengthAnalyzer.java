package de.unileipzig.dbs.pprl.core.analyzer.tags.record;

import de.unileipzig.dbs.pprl.core.analyzer.tags.attribute.StringLengthAttributeAnalyzer;
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.monitoring.Tag;

import java.util.List;
import java.util.Optional;

import static de.unileipzig.dbs.pprl.core.analyzer.tags.attribute.StringLengthAttributeAnalyzer.LEN;


public class RecordLengthAnalyzer implements RecordAnalyzer {

  public static final String RECORD_LENGTH = "ALL_" + LEN;

  @Override
  public List<Tag> getTags(Record record) {
    long len = 0;
    for (String attributeName : record.getAttributeNames()) {
      Optional<Attribute> attribute = record.getAttribute(attributeName);
      if (attribute.isPresent()) {
        long curLength = StringLengthAttributeAnalyzer.getLength(
          attribute.get().getAsString()
        );
        len += curLength;
      }
    }
    // Round to multiples of 5
    long roundedLen = (len / 5) * 5;
    return List.of(Tag.create(
        record,
        null,
        RECORD_LENGTH,
        String.valueOf(roundedLen),
        (double) len
    ));
  }

}

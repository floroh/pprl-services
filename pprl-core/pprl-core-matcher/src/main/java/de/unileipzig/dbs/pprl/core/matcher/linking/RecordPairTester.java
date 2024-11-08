package de.unileipzig.dbs.pprl.core.matcher.linking;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;

import java.util.Optional;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property = "@class")
public interface RecordPairTester extends java.util.function.Predicate<RecordPair> {

  boolean test(RecordPair recordPair);

  default Optional<RecordPair> collect(RecordPair recordPair) {
    return test(recordPair) ? Optional.of(recordPair) : Optional.empty();
  }
}

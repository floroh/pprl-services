package de.unileipzig.dbs.pprl.core.common.validation.api;

public interface ValidationResult {

  boolean isValid();

  default String getMessage() {
    return isValid() ? "VALID" : "INVALID";
  }
}

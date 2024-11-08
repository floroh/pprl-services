package de.unileipzig.dbs.pprl.core.common.validation.impl;

import de.unileipzig.dbs.pprl.core.common.validation.api.ValidationResult;
import de.unileipzig.dbs.pprl.core.common.validation.api.ValidationResultEntry;

import java.util.ArrayList;
import java.util.List;

public class DetailedValidationResult extends BooleanValidationResult implements ValidationResult {

  private final List<ValidationResultEntry> entries;

  public DetailedValidationResult(boolean isValid, List<ValidationResultEntry> entries) {
    super(isValid);
    this.entries = entries;
  }

  public DetailedValidationResult(boolean isValid) {
    super(isValid);
    this.entries = new ArrayList<>();
  }

  public void addEntry(ValidationResultEntry entry) {
    entries.add(entry);
  }

  public List<ValidationResultEntry> getEntries() {
    return entries;
  }

  @Override
  public String getMessage() {
    //TODO Include entry.asReadableMessage()
    return super.getMessage();
  }
}

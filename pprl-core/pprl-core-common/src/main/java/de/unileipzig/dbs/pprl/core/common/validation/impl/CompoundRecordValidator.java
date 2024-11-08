package de.unileipzig.dbs.pprl.core.common.validation.impl;

import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.validation.api.RecordValidator;
import de.unileipzig.dbs.pprl.core.common.validation.api.ValidationResult;

import java.util.ArrayList;
import java.util.List;

public class CompoundRecordValidator implements RecordValidator {

  private List<RecordValidator> validators;

  public CompoundRecordValidator(
    List<RecordValidator> validators) {
    this.validators = validators;
  }

  public CompoundRecordValidator() {
    validators = new ArrayList<>();
  }

  @Override
  public ValidationResult validate(Record record) {
    DetailedValidationResult result = new DetailedValidationResult(true);
    for (RecordValidator validator : validators) {
      ValidationResult curResult = validator.validate(record);
      result.setValid(result.isValid() && curResult.isValid());
      if (curResult instanceof BooleanValidationResult) {
        result.setHasReport(result.hasReport() || ((BooleanValidationResult) curResult).hasReport());
      }
      if (curResult instanceof DetailedValidationResult) {
        ((DetailedValidationResult) curResult).getEntries().forEach(result::addEntry);
      }
    }
    return result;
  }

  public void addRecordValidator(RecordValidator recordValidator) {
    validators.add(recordValidator);
  }
}

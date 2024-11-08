package de.unileipzig.dbs.pprl.core.common.validation.impl;

import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.validation.api.RecordValidator;
import de.unileipzig.dbs.pprl.core.common.validation.api.ValidationResult;

import java.util.List;
import java.util.Optional;

public class AvailabilityValidator implements RecordValidator {

  public static final String MISSING = "field.missing";
  public static final String EMPTY = "field.empty";

  private final List<String> requiredAttributes;

  private boolean invalidIfEmpty = true;

  public AvailabilityValidator(List<String> requiredAttributes) {
    this.requiredAttributes = requiredAttributes;
  }

  @Override
  public ValidationResult validate(Record record) {
    final DetailedValidationResult result = new DetailedValidationResult(true);

    for (String requiredAttribute : requiredAttributes) {
      Optional<Attribute> attribute = record.getAttribute(requiredAttribute);
      if (attribute.isEmpty()) {
        result.setValid(false);
        result.addEntry(new FieldErrorCode(MISSING, requiredAttribute));
      } else if (isEmpty(attribute.get())) {
        if (invalidIfEmpty) {
          result.setValid(false);
        } else {
          result.setHasReport(true);
        }
        result.addEntry(new FieldErrorCode(EMPTY, requiredAttribute));
      }
    }
    return result;
  }

  public static boolean isEmpty(Attribute attribute) {
    if (attribute.isString()) {
      return attribute.getAsString()
        .isEmpty();
    }
    return false;
  }

  public boolean checkIfEmpty() {
    return invalidIfEmpty;
  }

  public void setInvalidIfEmpty(boolean invalidIfEmpty) {
    this.invalidIfEmpty = invalidIfEmpty;
  }
}

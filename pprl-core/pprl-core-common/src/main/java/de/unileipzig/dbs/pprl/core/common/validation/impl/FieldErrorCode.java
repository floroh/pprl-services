package de.unileipzig.dbs.pprl.core.common.validation.impl;

import de.unileipzig.dbs.pprl.core.common.validation.api.ValidationResultEntry;

public class FieldErrorCode implements ValidationResultEntry {

  private String errorCode;

  private String field;

  private String message;

  public FieldErrorCode(String errorCode, String field, String message) {
    this.errorCode = errorCode;
    this.field = field;
    this.message = message;
  }

  public FieldErrorCode(String errorCode, String field) {
    this.errorCode = errorCode;
    this.field = field;
  }

  @Override
  public String asReadableMessage() {
    return "Field " + field + " has error " + errorCode + (message == null ? " (" + message + ")" : "");
  }

  public String getErrorCode() {
    return errorCode;
  }

  public String getField() {
    return field;
  }

  public String getMessage() {
    return message;
  }
}

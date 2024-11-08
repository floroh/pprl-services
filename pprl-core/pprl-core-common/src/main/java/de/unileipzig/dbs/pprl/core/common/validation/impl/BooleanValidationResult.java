package de.unileipzig.dbs.pprl.core.common.validation.impl;

import de.unileipzig.dbs.pprl.core.common.validation.api.ValidationResult;

public class BooleanValidationResult implements ValidationResult {

  private boolean isValid;

  private boolean hasReport;

  public BooleanValidationResult(boolean isValid) {
    this.isValid = isValid;
  }

  @Override
  public boolean isValid() {
    return isValid;
  }

  public void setValid(boolean valid) {
    isValid = valid;
  }

  public boolean hasReport() {
    return hasReport;
  }

  public void setHasReport(boolean hasReport) {
    this.hasReport = hasReport;
  }
}

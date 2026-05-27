package de.unileipzig.dbs.pprl.core.common.exceptions;

public class PprlException extends RuntimeException {
  public PprlException(String message) {
    super(message);
  }
  public PprlException(String message, Throwable cause) {
    super(message, cause);
  }
}
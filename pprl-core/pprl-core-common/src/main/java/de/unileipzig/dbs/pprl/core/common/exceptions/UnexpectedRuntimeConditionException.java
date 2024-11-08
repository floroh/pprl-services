package de.unileipzig.dbs.pprl.core.common.exceptions;

/**
 * Exception thrown during execution due to an unexpected condition
 */
public class UnexpectedRuntimeConditionException extends RuntimeException {
  public UnexpectedRuntimeConditionException(String message) {
    super(message);
  }
}

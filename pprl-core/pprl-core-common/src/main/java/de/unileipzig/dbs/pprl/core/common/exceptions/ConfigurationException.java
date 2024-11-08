package de.unileipzig.dbs.pprl.core.common.exceptions;

/**
 * Exception thrown during execution due to a configuration error
 * e.g. a missing weight for a certain attribute
 */
public class ConfigurationException extends RuntimeException {
  public ConfigurationException(String message) {
    super(message);
  }
}

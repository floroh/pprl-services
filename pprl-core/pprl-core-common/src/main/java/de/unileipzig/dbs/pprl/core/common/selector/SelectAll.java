package de.unileipzig.dbs.pprl.core.common.selector;

/**
 * Dummy selector
 */
public class SelectAll<T> implements Selector<T> {
  @Override
  public boolean test(T object) {
    return true;
  }
}

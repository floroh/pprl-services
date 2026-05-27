package de.unileipzig.dbs.pprl.core.common.selector;

import java.util.List;

public class SelectorCombination<T> implements Selector<T> {

  public enum Operation {
    AND,
    OR
  }

  private Operation operation;

  private List<Selector<T>> selectors;

  public SelectorCombination(
    Operation operation, Selector<T>... selectors) {
    this(operation, List.of(selectors));
  }

  public SelectorCombination(
    Operation operation, List<Selector<T>> selectors) {
    this.operation = operation;
    this.selectors = selectors;
  }

  private SelectorCombination() {
  }

  @Override
  public boolean test(T t) {
    boolean result = setInitialResultState();
    for (Selector<T> selector : selectors) {
      result = combine(result, selector.test(t));
    }
    return result;
  }

  /**
   * Set initial result based on the operation
   * @return true for Operation.AND; false for Operation.OR
   */
  private boolean setInitialResultState() {
    return operation.equals(Operation.AND);
  }

  private boolean combine(boolean result0, boolean result1) {
    return switch (operation) {
      case AND -> result0 && result1;
      case OR -> result0 || result1;
    };
  }

  public Operation getOperation() {
    return operation;
  }

  public List<Selector<T>> getSelectors() {
    return selectors;
  }
}

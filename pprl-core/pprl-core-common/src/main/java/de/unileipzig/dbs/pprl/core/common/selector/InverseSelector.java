package de.unileipzig.dbs.pprl.core.common.selector;

public class InverseSelector<T> implements Selector<T> {

  private final Selector<T> selector;

  public InverseSelector(Selector<T> selector) {
    this.selector = selector;
  }

  @Override
  public boolean test(T t) {
    return !selector.test(t);
  }

  public Selector<T> getSelector() {
    return selector;
  }
}

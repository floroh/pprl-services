package de.unileipzig.dbs.pprl.core.analyzer.cluster.data;

public class Pair<T> {
  protected T v0;
  protected T v1;

  public Pair(T v0, T v1) {
    this.v0 = v0;
    this.v1 = v1;
  }

  public T getV0() {
    return v0;
  }

  public T getV1() {
    return v1;
  }
}

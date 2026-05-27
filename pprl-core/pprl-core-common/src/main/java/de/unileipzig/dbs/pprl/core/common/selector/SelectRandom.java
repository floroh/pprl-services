package de.unileipzig.dbs.pprl.core.common.selector;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Random;

public class SelectRandom<T> implements Selector<T> {

  private Double trueShare;

  private long seed;

  @JsonIgnore
  private Random r;

  public SelectRandom(Double trueShare) {
    this(trueShare, 123);
  }

  public SelectRandom(Double trueShare, long seed) {
    this.trueShare = trueShare;
    this.seed = seed;
    this.r = new Random(seed);
  }

  private SelectRandom() {
  }

  @Override
  public boolean test(Object object) {
    return (r.nextInt(100) < trueShare * 100);
  }

  public Double getTrueShare() {
    return trueShare;
  }

  public long getSeed() {
    return seed;
  }
}
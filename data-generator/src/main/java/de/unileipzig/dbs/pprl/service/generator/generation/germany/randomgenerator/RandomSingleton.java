package de.unileipzig.dbs.pprl.service.generator.generation.germany.randomgenerator;

import lombok.Getter;

import java.nio.charset.StandardCharsets;
import java.util.Random;


@Getter
public class RandomSingleton {

  private static final RandomSingleton INSTANCE = new RandomSingleton();

  // Expose the Random instance
  private final Random random;

  private RandomSingleton() {
    this.random = new Random();
  }

  public static RandomSingleton getInstance() {
    return INSTANCE;
  }

  public static Random getRandom() {
    return getInstance().random;
  }

  public void setSeed(long seed) {
    random.setSeed(seed);
  }

  public void setSeed(String seed) {
    if (seed == null) {
      throw new IllegalArgumentException("Seed string cannot be null");
    }
    long hash = hashStringToLong(seed);
    random.setSeed(hash);
  }

  private long hashStringToLong(String str) {
    byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
    long hash = 0;
    for (byte b : bytes) {
      hash = 31 * hash + (b & 0xff);
    }
    return hash;
  }
}

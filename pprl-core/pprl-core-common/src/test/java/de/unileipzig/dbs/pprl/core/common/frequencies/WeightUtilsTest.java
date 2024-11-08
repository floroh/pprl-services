package de.unileipzig.dbs.pprl.core.common.frequencies;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WeightUtilsTest {

  @Test
  void getWeightM() {
    double weightM = WeightUtils.getWeightM(0.00000001, 0.001);
    System.out.println(weightM);
    assertTrue(weightM != Double.NEGATIVE_INFINITY);
  }
}
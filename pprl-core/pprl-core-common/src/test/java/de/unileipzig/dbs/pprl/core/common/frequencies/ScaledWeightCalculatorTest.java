package de.unileipzig.dbs.pprl.core.common.frequencies;

import de.unileipzig.dbs.pprl.core.common.model.impl.PersonalAttributeType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ScaledWeightCalculatorTest {

  public static final String FN = PersonalAttributeType.FIRSTNAME.asString();
  private static Map<String, Long> firstNameFrequencies;

  private static AttributesFrequencyLookup afl;

  private static Map<String, Double> defaultWeights;

  @BeforeAll
  static void beforeAll() {
    afl = new AttributesFrequencyLookup(false);

    firstNameFrequencies = Map.ofEntries(
      Map.entry(afl.normalizeAttributeValue(FN, "PETER"), 100L),
      Map.entry(afl.normalizeAttributeValue(FN, "MICHAEL"), 90L),
      Map.entry(afl.normalizeAttributeValue(FN, "MARIA"), 100L),
      Map.entry(afl.normalizeAttributeValue(FN, "EDDA"), 6L),
      Map.entry(afl.normalizeAttributeValue(FN, "ILKA"), 5L)
    );
    afl.addAttributeFrequencyLookup(FN, new AttributeFrequencyLookup(firstNameFrequencies));

    defaultWeights = Map.of(
      FN, 12.0,
      PersonalAttributeType.DATEOFBIRTH.asString(), 15.0,
      PersonalAttributeType.CITY.asString(), 5.0,
      PersonalAttributeType.PLZ.asString(), 5.0
    );
  }

  @Test
  void commonVsRareVsUnknown() {
    ScaledWeightCalculator scaledWeightCalculator = new ScaledWeightCalculator(afl, defaultWeights);

    double weightCommon = scaledWeightCalculator.getWeight(FN, "Peter");
    double weightRare = scaledWeightCalculator.getWeight(FN, "Edda");
    double weightUnknown = scaledWeightCalculator.getWeight(FN, "xyz");
    assertTrue(weightCommon < weightRare);
    assertEquals(defaultWeights.get(FN), weightUnknown);
    assertTrue(weightCommon < weightUnknown);
    assertTrue(weightRare > weightUnknown);
  }
}
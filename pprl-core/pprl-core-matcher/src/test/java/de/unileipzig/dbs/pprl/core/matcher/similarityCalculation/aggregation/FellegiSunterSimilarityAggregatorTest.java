package de.unileipzig.dbs.pprl.core.matcher.similarityCalculation.aggregation;

import de.unileipzig.dbs.pprl.core.common.factories.AttributeFactory;
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.matcher.MatcherTestBase;
import de.unileipzig.dbs.pprl.core.matcher.model.AttributePairWithSimilarity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FellegiSunterSimilarityAggregatorTest extends MatcherTestBase {

  private List<AttributePairWithSimilarity> attributePairWithSimilarities;
  private Map<String, Double> mWeights;
  private Map<String, Double> uWeights;

  @BeforeEach
  void setUp() {
    attributePairWithSimilarities = new ArrayList<>();
    mWeights = new HashMap<>();
    uWeights = new HashMap<>();
    List<Double> sims = Arrays.asList(0.6, 0.6, 0.0, 0.8, 1.0, 1.0);
    List<Double> mWeightList = Arrays.asList(1.0, 2.0, 2.0, 1.0, 0.5, 0.5);
    List<Double> uWeightList = Arrays.asList(-2.0, -1.0, -1.0, -0.5, -0.5, -0.5);
    for (int i = 0; i < sims.size(); i++) {
      attributePairWithSimilarities.add(
              new AttributePairWithSimilarity("DUMMY_" + i, getAttr(getRandomBitVector(32)),
                      getAttr(getRandomBitVector(32)), sims.get(i)
              ));
      mWeights.put("DUMMY_" + i, mWeightList.get(i));
      uWeights.put("DUMMY_" + i, uWeightList.get(i));
    }
  }

  @Test
  void aggregate() {
    FellegiSunterSimilarityAggregator similarityAggregator = new FellegiSunterSimilarityAggregator(mWeights, uWeights);
    double result = similarityAggregator.aggregate(attributePairWithSimilarities);
    assertEquals(0.544, result, 0.00001);
  }

  private Attribute getAttr(Object obj) {
    return AttributeFactory.getAttribute(obj);
  }
}
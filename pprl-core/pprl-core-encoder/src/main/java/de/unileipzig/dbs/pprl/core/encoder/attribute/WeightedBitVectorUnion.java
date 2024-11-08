/*
 * Copyright © 2018 - 2021 Leipzig University (Database Research Group)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.unileipzig.dbs.pprl.core.encoder.attribute;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.unileipzig.dbs.pprl.core.common.frequencies.DefaultWeightCalculator;
import de.unileipzig.dbs.pprl.core.common.frequencies.WeightCalculator;
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.BitVector;
import de.unileipzig.dbs.pprl.core.common.model.impl.BitSetVector;
import de.unileipzig.dbs.pprl.core.encoder.hardening.Permutation;

import java.math.BigInteger;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Merge multiple {@link BitVector}s into one while taking their weights into account
 * 1) calculate attribute weights
 * 2) calculate corresponding share in joint bloom filter
 * 3) sample bits of attributes into joint bloom filter
 * 4) permute joint bloom filter
 */
public class WeightedBitVectorUnion implements AttributeMerger<BitVector> {

  private WeightCalculator weightCalculator;

  private int length;

  private String seed = "DEFAULT_RBF_SEED";

  // TODO Replace this ugly workaround and include the plaintext attributes somehow in the merge method call
  @JsonIgnore
  private Map<String, Attribute> currentPlainAttributes;

  public WeightedBitVectorUnion(
    WeightCalculator weightCalculator, int length) {
    this.weightCalculator = weightCalculator;
    this.length = length;
  }

  private WeightedBitVectorUnion() {
  }

  @Override
  public BitVector merge(Map<String, BitVector> attributes) {

    // Determine weights
    Map<String, Double> weights = new HashMap<>();
    for (Map.Entry<String, BitVector> attribute : attributes.entrySet()) {
      String attributeName = attribute.getKey();
      Attribute plainAttribute = currentPlainAttributes.get(attributeName);
      if (plainAttribute == null) {
        Double weight = ((DefaultWeightCalculator) weightCalculator).getDefaultWeights().get(attributeName);
        weights.put(attributeName, weight);
      } else {
        String plainValue = plainAttribute.getAsString();
        double weight = weightCalculator.getWeight(attributeName, plainValue);
        weights.put(attributeName, weight);
      }
    }

    // Determine shares
//    final int bvLength = findBitVectorLength(attributes);
    int bitsLeft = length;
    Map<String, Double> shares = new HashMap<>(); // For control purposes only
    Map<String, Integer> numberOfBits = new HashMap<>();
    double totalWeight = 0;
    for (Double curWeight : weights.values()) {
      totalWeight += curWeight;
    }
    int remainingAttributes = weights.size();
    for (Map.Entry<String, Double> weightEntry : weights.entrySet()) {
      double curShare = weightEntry.getValue() / totalWeight;
      String attributeName = weightEntry.getKey();
      shares.put(attributeName, curShare);
      if (remainingAttributes == 1) {
        numberOfBits.put(attributeName, bitsLeft);
      } else {
        int curNumberOfBits = (int) Math.round(curShare * length);
        numberOfBits.put(attributeName, curNumberOfBits);
        bitsLeft -= curNumberOfBits;
        remainingAttributes--;
      }
    }
//    System.out.println("Determined shares for BitVectorUnion: " + shares);

    Map<String, Integer> numberOfSetBits = new HashMap<>(numberOfBits);
    // Sample
    BitVector bv = new BitSetVector(length);
    int outputPos = 0;
    for (Map.Entry<String, BitVector> attribute : attributes.entrySet()) {
      int setBits = 0;
      BitSet curBs = attribute.getValue().getBitSet();
      int curLength = attribute.getValue().getLength();
      int curNumberOfBits = numberOfBits.get(attribute.getKey());
      String secret = attribute.getKey() + seed;
      long seed = new BigInteger(secret.getBytes()).longValue();
      Random random = new Random(seed);
      for (int i = 0; i < curNumberOfBits; i++) {
        int curPos = random.nextInt(curLength);
        boolean curState = curBs.get(curPos);
        if (curState) setBits++;
        bv.set(outputPos, curState);
        outputPos++;
      }
      numberOfSetBits.put(attribute.getKey(), setBits);
    }
    StringBuilder sb = new StringBuilder("WeightedBitVectorUnion: ");
    for (String attribute : numberOfBits.keySet()) {
      Integer bitCount = numberOfBits.get(attribute);
      Integer setBits = numberOfSetBits.get(attribute);
      int share = (int)(100.0 * setBits / bitCount);
      sb.append(" ")
        .append(attribute)
        .append("=").append(setBits).append("/").append(bitCount).append("=")
        .append(share);
    }
    System.out.println(sb);
//    System.out.println("Determined bit shares for BitVectorUnion: " + numberOfBits);

    // Permutation
    BitVector outputBv = Permutation.permuteBitVector(bv, "PERMUTATION_SEED");
    return outputBv;
  }

  private int findBitVectorLength(Map<String, BitVector> attributes) {
    List<Integer> bvLengths = attributes.values().stream()
      .map(BitVector::getLength)
      .distinct()
      .collect(Collectors.toList());
    if (bvLengths.size() > 1) {
      throw new RuntimeException("Trying to merge BitVectors of different length");
    }
    return bvLengths.getFirst();
  }

  public void setCurrentPlainAttributes(
    Map<String, Attribute> currentPlainAttributes) {
    this.currentPlainAttributes = currentPlainAttributes;
  }

  public WeightCalculator getWeightCalculator() {
    return weightCalculator;
  }

  public int getLength() {
    return length;
  }

  public String getSeed() {
    return seed;
  }

  public void setSeed(String seed) {
    this.seed = seed;
  }

  @Override
  public String toString() {
    return "WeightedBitVectorUnion";
  }
}

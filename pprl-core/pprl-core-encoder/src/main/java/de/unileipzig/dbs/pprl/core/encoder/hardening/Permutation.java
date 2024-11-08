package de.unileipzig.dbs.pprl.core.encoder.hardening;

import de.unileipzig.dbs.pprl.core.common.HashUtils;
import de.unileipzig.dbs.pprl.core.common.model.api.BitVector;
import de.unileipzig.dbs.pprl.core.common.model.impl.BitSetVector;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

public class Permutation implements KeyedHardener<BitVector> {

  @Override
  public BitVector harden(BitVector original, String key) {
    return permuteBitVector(original, key);
  }

  public static BitVector permuteBitVector(BitVector original, String key) {
    BitSet bitSet = original.getBitSet();
    int bvLength = original.getLength();

    final List<Boolean> bits = toBitList(bitSet, bvLength);
    Collections.shuffle(bits, HashUtils.getRandom(key, key));
    BitSet newBitSet = toBitSet(bits, bvLength);
    return BitSetVector.fromBitSet(newBitSet, bvLength);
  }

  private static List<Boolean> toBitList(BitSet bitSet, int bvLength) {
    final ArrayList<Boolean> bits = new ArrayList<>();
    for (int i = 0; i < bvLength; i++) {
      bits.add(bitSet.get(i));
    }
    return bits;
  }

  private static BitSet toBitSet(List<Boolean> bits, int bvLength) {
    BitSet newBitSet = new BitSet(bvLength);
    for (int i = 0; i < bvLength; i++) {
      if (bits.get(i)) {
        newBitSet.set(i);
      }
    }
    return newBitSet;
  }
}

package de.unileipzig.dbs.pprl.core.encoder.hardening;

import de.unileipzig.dbs.pprl.core.common.BitSetUtils;
import de.unileipzig.dbs.pprl.core.common.model.api.BitVector;
import de.unileipzig.dbs.pprl.core.common.model.impl.BitSetVector;
import org.junit.jupiter.api.Test;

import java.util.BitSet;

import static org.junit.jupiter.api.Assertions.*;

class PermutationTest {

  @Test
  void harden() {
    BitSet bs = BitSetUtils.generateRandomBitSet(128, 56);
    BitVector bv = BitSetVector.fromBitSet(bs, 128);

    Permutation hardener = new Permutation();
    BitVector hbv0 = hardener.harden(bv, "key0");
    assertEquals(bv.getCardinality(), hbv0.getCardinality());

    BitVector hbv1 = hardener.harden(bv, "key87654");
    assertEquals(bv.getCardinality(), hbv1.getCardinality());

    assertNotEquals(hbv0, hbv1);
//    System.out.println(bv.getBitString());
//    System.out.println(hbv0.getBitString());
//    System.out.println(hbv1.getBitString());
  }
}
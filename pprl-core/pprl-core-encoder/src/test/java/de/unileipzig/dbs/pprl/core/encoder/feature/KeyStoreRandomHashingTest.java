package de.unileipzig.dbs.pprl.core.encoder.feature;

import de.unileipzig.dbs.pprl.core.common.model.api.BitVector;
import de.unileipzig.dbs.pprl.core.encoder.KeyManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

class KeyStoreRandomHashingTest {

  private static final String KEYNAME1 = "bf-seed1";
  private static final String KEYNAME2 = "bf-seed2";
  private static final byte[] BASEKEY = "SUPER-base-secret".getBytes();

  @Test
  void encode() {
    prepareKeyStore();

    List<String> features = Arrays.asList("ab", "bc", "cd", "de");
    KeyStoreRandomHashing encoder1 = new KeyStoreRandomHashing(16, KEYNAME1);
    KeyStoreRandomHashing encoder2 = new KeyStoreRandomHashing(16, KEYNAME2);
    KeyStoreRandomHashing encoder3 = new KeyStoreRandomHashing(16, KEYNAME1);

    for (String feature : features) {
      BitVector bv1 = encoder1.encode(64, feature);
      BitVector bv2 = encoder2.encode(64, feature);
      BitVector bv3 = encoder3.encode(64, feature);
      Assertions.assertNotEquals(bv1.getBitSet(), bv2.getBitSet());
      Assertions.assertEquals(bv1.getBitSet(), bv3.getBitSet());
    }
  }

  @Test
  void encodeWithAdditionalKey() {
    prepareKeyStore();

    List<String> features = Arrays.asList("ab", "bc", "cd", "de");
    KeyStoreRandomHashing encoder1 = new KeyStoreRandomHashing(16, KEYNAME1);
    encoder1.setKey("RECORD-SPECIFIC-KEY_1");
    KeyStoreRandomHashing encoder2 = new KeyStoreRandomHashing(16, KEYNAME1);
    encoder2.setKey("RECORD-SPECIFIC-KEY_2");
    KeyStoreRandomHashing encoder3 = new KeyStoreRandomHashing(16, KEYNAME1);
    encoder3.setKey(null);

    for (String feature : features) {
      BitVector bv1 = encoder1.encode(64, feature);
      BitVector bv2 = encoder2.encode(64, feature);
      BitVector bv3 = encoder3.encode(64, feature);
      Assertions.assertNotEquals(bv1.getBitSet(), bv2.getBitSet());
      Assertions.assertNotEquals(bv1.getBitSet(), bv3.getBitSet());
    }
  }

  private void prepareKeyStore() {
    KeyManager.addSecret(KeyManager.BASE_KEY_NAME, BASEKEY, KeyManager.SECRET_PWD_ARRAY);
  }
}
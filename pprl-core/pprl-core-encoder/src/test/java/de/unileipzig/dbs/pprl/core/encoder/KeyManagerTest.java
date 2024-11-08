package de.unileipzig.dbs.pprl.core.encoder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import static org.junit.jupiter.api.Assertions.*;

class KeyManagerTest {

  @BeforeEach
  void init() {
    KeyManager.setKeyStore(KeyManager.initEmptyKeyStore());
  }

  @Test
  void getWhenEmpty() {
    KeyStore keyStore = KeyManager.getKeyStore();
    assertNotNull(keyStore);

    assertDoesNotThrow(() -> KeyManager.getKeyStore().getKey("unknownAlias", null));

    try {
      assertNull(KeyManager.getKeyStore().getKey("unknownAlias", null));
    } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
      e.printStackTrace();
    }
  }

  @Test
  void writeAndRead() throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {
    String keyName = "bf-seed";
    String key = "SUPER-secret";
    String password = "secretPW";

    byte[] binaryKey = key.getBytes();
    KeyManager.addSecret(keyName, binaryKey, password.toCharArray());

    Key retrievedKey = KeyManager.getKeyStore().getKey(keyName, password.toCharArray());
    String retrievedKeyValue = new String(retrievedKey.getEncoded());
    assertEquals(key, retrievedKeyValue);

    char[] wrongPassword = "otherPW".toCharArray();
    assertThrows(UnrecoverableKeyException.class, () -> KeyManager.getKeyStore().getKey(keyName,
      wrongPassword));
  }

  @Test
  void getDerivedFromUnknownBase() throws KeyStoreException {
    assertEquals(0, KeyManager.getKeyStore().size());
    assertThrows(RuntimeException.class, () -> KeyManager.getDerivedSecret("subkeyName"));
  }
}
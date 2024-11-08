package de.unileipzig.dbs.pprl.core.encoder;

import de.unileipzig.dbs.pprl.core.encoder.crypto.SecretDerivation;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public class KeyManager {
  public static final String BASE_KEY_NAME = "BASE_KEY";
  public static final char[] SECRET_PWD_ARRAY = "ksPass".toCharArray();

  private static KeyStore keyStore;

  private static final SecretDerivation secretDerivation = new SecretDerivation();

  public static KeyStore getKeyStore() {
    if (keyStore == null) {
      keyStore = initEmptyKeyStore();
    }
    return keyStore;
  }

  public static void setKeyStore(KeyStore keyStore) {
    KeyManager.keyStore = keyStore;
  }

  public static void addSecret(String keyName, byte[] key, char[] password) {
    addSecret(getKeyStore(), keyName, key, password);
  }

  public static void addSecret(KeyStore keystore, String keyName, byte[] key, char[] password) {
    SecretKey secretKey = new SecretKeySpec(key, "AES");
    KeyStore.SecretKeyEntry secret = new KeyStore.SecretKeyEntry(secretKey);
    KeyStore.PasswordProtection passwordProtection = new KeyStore.PasswordProtection(password);
    try {
      keystore.setEntry(keyName, secret, passwordProtection);
    } catch (KeyStoreException e) {
      throw new RuntimeException(e.fillInStackTrace());
    }
  }

  public static SecretKey getDerivedSecret(String keyName) {
    Key baseKey = null;
    try {
      baseKey = getKeyStore().getKey(BASE_KEY_NAME, SECRET_PWD_ARRAY);
    } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
      throw new RuntimeException(e.fillInStackTrace());
    }
    if (baseKey == null) {
      throw new RuntimeException("Missing key for alias: " + BASE_KEY_NAME);
    }
    Key saltKey = new SecretKeySpec(keyName.getBytes(), "AES");
    return secretDerivation.deriveSecret(baseKey, saltKey);
  }

  public static KeyStore initEmptyKeyStore() {
    try {
      KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
      keyStore.load(null, null);
      return keyStore;
    } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
      throw new RuntimeException(e.fillInStackTrace());
    }
  }
}

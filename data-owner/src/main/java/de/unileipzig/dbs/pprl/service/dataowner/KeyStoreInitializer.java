package de.unileipzig.dbs.pprl.service.dataowner;

import de.unileipzig.dbs.pprl.core.encoder.KeyManager;

import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class KeyStoreInitializer {

  public static void main(String[] args) throws KeyStoreException, CertificateException, IOException,
    NoSuchAlgorithmException {
    KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
    char[] pwdArray = "secret-ks-password".toCharArray();
    keyStore.load(null, pwdArray);

    KeyManager.addSecret(
      keyStore,
      KeyManager.BASE_KEY_NAME,
      "exampleProject".getBytes(),
      "ksPass".toCharArray()
    );
    try (FileOutputStream fos = new FileOutputStream("pprl-data-owner.jks")) {
      keyStore.store(fos, pwdArray);
    }
  }

}

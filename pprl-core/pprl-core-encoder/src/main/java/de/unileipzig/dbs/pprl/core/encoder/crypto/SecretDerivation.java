package de.unileipzig.dbs.pprl.core.encoder.crypto;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

public class SecretDerivation {

  public static final int ITERATION_COUNT = 10000;
  public static final int KEY_LENGTH = 256;
  public static final String SECRET_KEY_ALGORITHM = "PBKDF2WithHmacSHA256";

  private final SecretKeyFactory skf;
  private static final Base64.Encoder b64encoder = Base64.getEncoder();

  public SecretDerivation() {
    try {
      skf = SecretKeyFactory.getInstance(SECRET_KEY_ALGORITHM);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("Missing secret key algorithm: " + SECRET_KEY_ALGORITHM);
    }
  }

  public SecretKey deriveSecret(Key password, Key salt) {
    char[] passwordChars = b64encoder.encodeToString(password.getEncoded()).toCharArray();
    byte[] saltBytes = salt.getEncoded();
    return deriveSecret(passwordChars, saltBytes);
  }

  public SecretKey deriveSecret(char[] password, byte[] salt) {
    KeySpec keySpec = new PBEKeySpec(password, salt, ITERATION_COUNT, KEY_LENGTH);
    SecretKey secretKey = null;
    try {
      secretKey = skf.generateSecret(keySpec);
    } catch (InvalidKeySpecException e) {
      throw new RuntimeException(e.fillInStackTrace());
    }
    return secretKey;
  }
}

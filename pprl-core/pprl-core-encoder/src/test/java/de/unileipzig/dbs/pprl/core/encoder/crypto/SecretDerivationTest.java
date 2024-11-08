package de.unileipzig.dbs.pprl.core.encoder.crypto;

import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class SecretDerivationTest {

  @Test
  void reproducibility() {
    String password = "PROJECT_PASSWORD";
    String salt = "SALT_FN";

    String child0 = deriveSecret(password, salt);
    String child1 = deriveSecret(password, salt);
//    System.out.println("0: " + child0 + ", 1: " + child1);
    assertEquals(child0, child1);
  }

  @Test
  void differentPasswords() {
    String password0 = "PROJECT_PASSWORD_0";
    String password1 = "PROJECT_PASSWORD_1";
    String salt = "SALT_FN";

    String child0 = deriveSecret(password0, salt);
    String child1 = deriveSecret(password1, salt);
//    System.out.println("0: " + child0 + ", 1: " + child1);
    assertNotEquals(child0, child1);
  }

  @Test
  void differentSalts() {
    String password = "PROJECT_PASSWORD";
    String salt0 = "SALT_FN";
    String salt1 = "SALT_DOB";

    String child0 = deriveSecret(password, salt0);
    String child1 = deriveSecret(password, salt1);
//    System.out.println("0: " + child0 + ", 1: " + child1);
    assertNotEquals(child0, child1);
  }

  public static String deriveSecret(String password, String salt) {
    SecretDerivation secretDerivation = new SecretDerivation();
    char[] passwordChars = password.toCharArray();
    byte[] saltBytes = salt.getBytes();
    SecretKey secretKey = secretDerivation.deriveSecret(passwordChars, saltBytes);
    return Base64.getEncoder().encodeToString(secretKey.getEncoded());
  }
}
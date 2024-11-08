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

package de.unileipzig.dbs.pprl.core.common;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.BitSet;
import java.util.Random;

import static javax.xml.crypto.dsig.SignatureMethod.HMAC_SHA512;

/**
 * Utility class for calculating hash values.
 *
 * @author mfranke
 */
public class HashUtils {
  private static final String MD5 = "MD5";
  private static final String SHA = "SHA-256";
  public static final String HMAC_ALGORITHM = "HmacSHA256";

  private HashUtils() {
    throw new RuntimeException();
  }

  public static int getHash(String algorithm, String input) {
    return getHash(algorithm, input.getBytes());
  }

  private static int getHash(String algorithm, byte[] bytes) {
    try {
      MessageDigest md = MessageDigest.getInstance(algorithm);
      byte[] messageDigest = md.digest(bytes);
      BigInteger number = new BigInteger(1, messageDigest);
      return number.intValue();
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Calculates the MD5 hash for a string input.
   *
   * @param input a String value.
   * @return the {@link int} representation of the MDH5 hash value.
   */
  public static int getMD5(String input) {
    return getMD5(input.getBytes());
  }

  /**
   * Calculates the MD5 hash for a BitSet input.
   *
   * @param input a {@link BitSet} object.
   * @return the {@link BitSet} representation of the MD5 hash value.
   */
  public static int getMD5(BitSet input) {
    return getMD5(input.toByteArray());
  }

  /**
   * Calculates the MD5 hash for a byte array
   *
   * @param bytes a byte array representation of the input.
   * @return the {@link int} representation of the MD5 hash value.
   */
  private static int getMD5(byte[] bytes) {
    try {
      MessageDigest md = MessageDigest.getInstance(MD5);
      byte[] messageDigest = md.digest(bytes);
      BigInteger number = new BigInteger(1, messageDigest);
      return number.intValue();
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Calculates the MD5 hash for a string input.
   *
   * @param input a String value.
   * @return the {@link int} representation of the MDH5 hash value.
   */
  public static int getSHA(String input) {
    return getSHA(input.getBytes());
  }

  /**
   * Calculates the MD5 hash for a BitSet input.
   *
   * @param input a {@link BitSet} object.
   * @return the {@link BitSet} representation of the MD5 hash value.
   */
  public static int getSHA(BitSet input) {
    return getSHA(input.toByteArray());
  }

  /**
   * Calculates the MD5 hash for a byte array
   *
   * @param bytes a byte array representation of the input.
   * @return the {@link int} representation of the MDH5 hash value.
   */
  private static int getSHA(byte[] bytes) {
    try {
      MessageDigest md = MessageDigest.getInstance(SHA);
      byte[] messageDigest = md.digest(bytes);
      BigInteger number = new BigInteger(1, messageDigest);
      return number.intValue();
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Calculates the SHA hash for a string input.
   *
   * @param input a String value.
   * @return the {@link long} representation of the SHA hash value.
   */
  public static long getSHALongHash(String input) {
    try {
      MessageDigest md = MessageDigest.getInstance(SHA);
      byte[] messageDigest = md.digest(input.getBytes());
      BigInteger number = new BigInteger(1, messageDigest);
      return number.longValue();
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  public static byte[] getHMacBytes(String data, String salt) {
    SecretKeySpec secretKeySpec = new SecretKeySpec(salt.getBytes(), HMAC_SHA512);
    try {
      Mac mac = Mac.getInstance(HMAC_ALGORITHM);
      mac.init(secretKeySpec);
      return mac.doFinal(data.getBytes());
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException();
    }
  }

  public static int getHMAC(String data, String key) {
    SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), HMAC_SHA512);
    try {
      Mac mac = Mac.getInstance(HMAC_ALGORITHM);
      mac.init(secretKeySpec);
      byte[] hash = mac.doFinal(data.getBytes());
      return new BigInteger(1, hash).intValue();
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to get Random instance for hashing");
    }
  }

  public static Random getRandom(String message, String salt) {
    return getRandom(message, salt.getBytes());
  }

  public static Random getRandom(String message, byte[] salt) {
    long seed;
    try {
      Mac mac = Mac.getInstance(HMAC_ALGORITHM);
      mac.init(new SecretKeySpec(salt, HMAC_ALGORITHM));
      byte[] hash = mac.doFinal(message.getBytes());
      seed = new BigInteger(hash).longValue();
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to get Random instance for hashing");
    }
    return new Random(seed);
  }
}
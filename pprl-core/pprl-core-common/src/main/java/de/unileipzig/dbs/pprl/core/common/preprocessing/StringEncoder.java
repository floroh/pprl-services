package de.unileipzig.dbs.pprl.core.common.preprocessing;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.language.Soundex;

import java.util.Map;

/**
 * Transform a String using an encoding, e.g. Soundex
 */
public class StringEncoder extends StringAttributePreprocessor {

  public enum EncoderType {
    Soundex
  }

  public static StringEncoder SOUNDEX = new StringEncoder(EncoderType.Soundex);

  private static final Map<EncoderType, org.apache.commons.codec.StringEncoder> encoder = Map.of(
    EncoderType.Soundex, new Soundex()
  );

  private final EncoderType encoderType;

  public StringEncoder(StringEncoder.EncoderType encoderType) {
    this.encoderType = encoderType;
  }

  @Override
  public String preprocess(String value) {
    try {
      return encoder.get(encoderType).encode(value);
    } catch (EncoderException e) {
      throw new RuntimeException(e);
    }
  }

  public EncoderType getEncoderType() {
    return encoderType;
  }
}

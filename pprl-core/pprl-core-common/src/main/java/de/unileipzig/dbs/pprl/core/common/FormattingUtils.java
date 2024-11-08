package de.unileipzig.dbs.pprl.core.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

public class FormattingUtils {

  public static String roundToString(Double d) {
    return roundToString(d, 2);
  }

  public static String roundToString(Double d, int precision) {
    return String.format(Locale.ENGLISH, "%." + precision + "f", d);
  }

  public static double roundToDouble(double input, int precision) {
    return BigDecimal.valueOf(input).setScale(precision, RoundingMode.HALF_UP).doubleValue();
  }

  public static String roundToString(int input, int multipleOf) {
    return String.valueOf((input / multipleOf) * multipleOf);
  }

  public static int roundToInt(int input, int multipleOf) {
    return (input / multipleOf) * multipleOf;
  }

}

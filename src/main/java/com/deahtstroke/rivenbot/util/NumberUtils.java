package com.deahtstroke.rivenbot.util;

import java.util.Objects;

public class NumberUtils {

  private NumberUtils() {
  }

  /**
   * Determines whether the given parameter is an Integer
   *
   * @param param the parameter to check
   * @return true if its an integer, else false
   */
  public static boolean isInteger(String param) {
    try {
      Integer.parseInt(param);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  /**
   * Determines whether the number contains the digit or not
   *
   * @param number the number to check
   * @param digit  the digit to find
   * @return true if it contains the digit, false if not
   */
  public static boolean containsDigit(Integer number, Integer digit) {
    if (number < 0) {
      return false;
    }
    while (number > 0) {
      int currentDigit = number % 10;
      if (Objects.equals(digit, currentDigit)) {
        return true;
      }
      number /= 10;
    }
    return false;
  }
}

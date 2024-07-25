package com.deahtstroke.rivenbot.util;

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
}
